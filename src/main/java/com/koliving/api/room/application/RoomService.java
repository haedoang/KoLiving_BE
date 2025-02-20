package com.koliving.api.room.application;

import static com.koliving.api.base.ServiceError.FORBIDDEN;
import static com.koliving.api.base.ServiceError.RECORD_NOT_EXIST;
import static com.koliving.api.base.ServiceError.UNAUTHORIZED;

import com.google.common.collect.Sets;
import com.koliving.api.base.ServiceError;
import com.koliving.api.base.exception.KolivingServiceException;
import com.koliving.api.email.IEmailService;
import com.koliving.api.file.domain.ImageFile;
import com.koliving.api.file.infra.ImageFileRepository;
import com.koliving.api.location.domain.Location;
import com.koliving.api.location.infra.LocationRepository;
import com.koliving.api.properties.FrontProperties;
import com.koliving.api.room.application.dto.RoomContactRequest;
import com.koliving.api.room.application.dto.RoomResponse;
import com.koliving.api.room.application.dto.RoomSaveRequest;
import com.koliving.api.room.application.dto.RoomSearchCondition;
import com.koliving.api.room.domain.Furnishing;
import com.koliving.api.room.domain.Like;
import com.koliving.api.room.domain.Room;
import com.koliving.api.room.infra.RoomContactEvent;
import com.koliving.api.room.infra.FurnishingRepository;
import com.koliving.api.room.infra.LikeRepository;
import com.koliving.api.room.infra.RoomRepository;
import com.koliving.api.user.domain.Notification;
import com.koliving.api.user.domain.User;
import com.koliving.api.user.infra.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * author : haedoang date : 2023/08/26 description :
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomService {

    private final FurnishingRepository furnishingRepository;
    private final LocationRepository locationRepository;
    private final RoomRepository roomRepository;
    private final ImageFileRepository imageFileRepository;
    private final LikeRepository likeRepository;
    private final IEmailService emailService;
    private final FrontProperties frontProperties;

    public List<RoomResponse> list() {
        return roomRepository.findAllWithUser()
            .stream()
            .map(RoomResponse::valueOf)
            .collect(Collectors.toList());
    }

    @Transactional
    public Long save(RoomSaveRequest request, User user) {
        Room room = request.toEntity(
            getLocationById(request.locationId()),
            getFurnishingsByIds(request.furnishingIds()),
            getImageFiles(request.imageIds())
        ).by(user);
        final Room savedRoom = roomRepository.save(room);

        return savedRoom.getId();
    }

    private Set<ImageFile> getImageFiles(Set<Long> imageIds) {
        List<ImageFile> images = imageFileRepository.findAllById(imageIds);

        if (images.size() != imageIds.size()) {
            throw new IllegalArgumentException();
        }
        return Sets.newHashSet(images);
    }

    private Set<Furnishing> getFurnishingsByIds(Set<Long> furnishingIds) {
        if (CollectionUtils.isEmpty(furnishingIds)) {
            return Collections.emptySet();
        }

        final List<Furnishing> furnishings = furnishingRepository.findAllById(furnishingIds);

        if (furnishings.size() != furnishingIds.size()) {
            throw new KolivingServiceException(ServiceError.RECORD_NOT_EXIST);
        }

        return Sets.newHashSet(furnishings);
    }

    private Location getLocationById(Long locationId) {
        final Location location = locationRepository.findById(locationId)
            .orElseThrow(() -> new KolivingServiceException(RECORD_NOT_EXIST));

        if (location.getLocationType().isTopLocation()) {
            throw new KolivingServiceException(ServiceError.INVALID_LOCATION);
        }

        return location;
    }

    public Page<RoomResponse> search(Pageable pageable, RoomSearchCondition condition) {
        return roomRepository.search(pageable, condition);
    }

    public RoomResponse findOne(Long id) {
        return RoomResponse.valueOf(getRoom(id));
    }

    @Transactional
    public void deleteRoomById(Long id) {
        Room room = getRoom(id);
        room.delete();
    }

    @Transactional
    public void deleteRoomById(Long id, User user) {
        Room room = getRoom(id);
        if (!room.checkUser(user)) {
            throw new KolivingServiceException(FORBIDDEN);
        }

        room.delete();
    }

    @Transactional
    public void likeRoom(Long roomId, User user) {
        Room room = getRoom(roomId);
        final Like like = Like.of(room, user);

        likeRepository.findByRoomIdAndUserId(roomId, user.getId())
            .ifPresentOrElse(
                likeRepository::delete,
                () -> likeRepository.save(like)
            );
        ;
    }

    public Page<RoomResponse> findLikeRoomByUser(Pageable pageable, User user) {
        return roomRepository.likedRooms(pageable, user.getId());
    }

    private Room getRoom(Long id) {
        return roomRepository.findByIdWithUser(id)
            .orElseThrow(() -> new KolivingServiceException(RECORD_NOT_EXIST));
    }

    @Transactional
    public void contact(RoomContactRequest request, User user) {
        final Room room = getRoom(request.roomId());
        final Notification notification = Notification.of(user, room.getUser());
        room.getUser().addReceivedNotification(notification);
        emailService.sendRoomContact(room.getUser().getEmail(), request.contactInfo(), request.message(), user, getRoomDetailUrl(room.getId()));
    }

    public String getRoomDetailUrl(Long roomId) {
        return String.format("%s/room/%d", frontProperties.getOrigin(), roomId);
    }
}
