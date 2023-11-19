package com.api.parkingcontrol.controllers;

import com.api.parkingcontrol.dtos.ParkingSpotDto;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class parkingSpotController {

    final ParkingSpotService ParkingSpotService;

    public parkingSpotController(com.api.parkingcontrol.services.ParkingSpotService parkingSpotService) {
        ParkingSpotService = parkingSpotService;
    }

    @PostMapping
    public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto ParkingSpotDto){
       if (ParkingSpotService.existsByLicensePlateCar(ParkingSpotDto.getLicensePlateCar())){
           return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: The License plate car is alreayd in use!");
       }
       if (ParkingSpotService.existsByParkingSpotNumber(ParkingSpotDto.getParkingSpotNumber())){
           return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: The parking spot number is already in use!");
       }
       if (ParkingSpotService.existsByApartmentAndBlock(ParkingSpotDto.getApartment(), ParkingSpotDto.getBlock())){
           return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking spot already resgistered for this apartment/block");
       }
        var parkingSpotModel = new ParkingSpotModel();
       BeanUtils.copyProperties(ParkingSpotDto, parkingSpotModel);
       parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
       return ResponseEntity.status(HttpStatus.CREATED).body(ParkingSpotService.save(parkingSpotModel));

    }

    @GetMapping
    public ResponseEntity<Page<ParkingSpotModel>> getAllParkingSpots(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)Pageable pageable){
        return ResponseEntity.status(HttpStatus.OK).body(ParkingSpotService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") UUID id){
        Optional<ParkingSpotModel> parkingSpotModelOptional = ParkingSpotService.findByid(id);
        if (!parkingSpotModelOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value = "id") UUID id){
        Optional<ParkingSpotModel> parkingSpotModelOptional = ParkingSpotService.findByid(id);
        if (!parkingSpotModelOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
        }
        ParkingSpotService.delete(parkingSpotModelOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("Parking spot was deleted successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") UUID id, @RequestBody @Valid ParkingSpotDto parkingSpotDto){
        Optional<ParkingSpotModel> parkingSpotModelOptional = ParkingSpotService.findByid(id);
        if (!parkingSpotModelOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
        }
        var parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setId(parkingSpotModelOptional.get().getId());
        parkingSpotModel.setRegistrationDate(parkingSpotModelOptional.get().getRegistrationDate());
        return ResponseEntity.status(HttpStatus.OK).body(ParkingSpotService.save(parkingSpotModel));
    }
}
