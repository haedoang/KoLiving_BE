package com.koliving.api.room.infra;

import com.koliving.api.room.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, Long>, RoomRepositoryQueryDsl {

    @Query("select r from TB_ROOM r join fetch r.user u")
    List<Room> findAllWithUser();

    @Query("select r from TB_ROOM r join fetch r.user u where r.id=:id")
    Optional<Room> findByIdWithUser(@Param("id") Long id);

}
