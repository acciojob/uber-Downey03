package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.CabRepository;
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
	@Autowired
	CabRepository cabRepository;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
//		Customer customer1 = new Customer();
//		customer1.setMobile(customer.getMobile());
//		customer1.setPassword(customer.getPassword());
//		customer1.setTripBookingList(new ArrayList<>());

		customerRepository2.save(customer);
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

		List<Cab> Cabs = cabRepository.findAll();

		System.out.println(Cabs.size());
		List<Cab> availableCabs = new ArrayList<>();
		for(Cab cab : Cabs){
			if(cab.getAvailable()) availableCabs.add(cab);
		}

		System.out.println(availableCabs.size());
		if(availableCabs.size()==0) throw new Exception("No cab available!");


		TripBooking tripBooking = new TripBooking();

		Customer customer = customerRepository2.findById(customerId).get();

		tripBooking.setCustomer(customer);
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setStatus(TripStatus.CONFIRMED);

		Driver driver = null;

		for(Cab cab : availableCabs){
			if(driver == null || driver.getDriverId() > cab.getDriver().getDriverId()){

				driver = cab.getDriver();
			}
		}
		if(driver == null) throw new Exception("No cab Available!");

		tripBooking.setDriver(driver);
		driver.getCab().setAvailable(false);
		driver.getTripBookingList().add(tripBooking);
		driverRepository2.save(driver);

		return  tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBooking.setBill(0);
		tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBooking.setBill(tripBooking.getDistanceInKm()*tripBooking.getDriver().getCab().getPerKmRate());
		tripBookingRepository2.save(tripBooking);

	}
}
