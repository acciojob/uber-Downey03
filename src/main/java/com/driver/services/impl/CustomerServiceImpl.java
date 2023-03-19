package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		Customer customer1 = new Customer();
		customer1.setMobile(customer.getMobile());
		customer1.setPassword(customer.getPassword());
		customer1.setTripBookingList(new ArrayList<>());

		customerRepository2.save(customer1);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer = customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> drivers = driverRepository2.findAll();
		if(drivers.size() == 0) throw new RuntimeException();

		TripBooking tripBooking = new TripBooking();

		Customer customer = customerRepository2.findById(customerId).get();

		tripBooking.setCustomer(customer);
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setStatus(TripStatus.CONFIRMED);

		List<Driver> availableDrivers = new ArrayList<>();

		for(Driver driver : drivers){
			Cab cab =driver.getCab();
			if(cab.isAvailable()) availableDrivers.add(driver);
		}

		Driver driver = new Driver();

		for(Driver driver1 : availableDrivers){
			if(driver1.getDriverId() < driver.getDriverId()) driver = driver1;
		}

		tripBooking.setDriver(driver);
		tripBooking.setBill(0);

		return  tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		List<Driver> driverList = driverRepository2.findAll();
		Driver theDriver = new Driver();
		boolean driverFound = false;
		for(Driver driver : driverList){
			List<TripBooking> tripBookingList = driver.getTripBookingList();

			for(TripBooking trip :tripBookingList){
				if(trip.getTripBookingId() == tripId){
					trip.setStatus(TripStatus.CANCELED);
					theDriver = driver;

					driverFound = true;
					break;
				}
			}
			if(driverFound) break;
		}
		Cab cab = theDriver.getCab();
		cab.setAvailable(true);

		driverRepository2.save(theDriver);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		List<Driver> driverList = driverRepository2.findAll();
		Driver theDriver = new Driver();
		boolean driverFound = false;
		for(Driver driver : driverList){
			List<TripBooking> tripBookingList = driver.getTripBookingList();

			for(TripBooking trip :tripBookingList){
				if(trip.getTripBookingId() == tripId){
					trip.setStatus(TripStatus.COMPLETED);
					theDriver = driver;

					driverFound = true;
					break;
				}
			}
			if(driverFound) break;
		}

		Cab cab = theDriver.getCab();
		cab.setAvailable(true);

		driverRepository2.save(theDriver);

	}
}
