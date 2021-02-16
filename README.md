# AirportQueuingProblem
A Java implementation of a cost function for mathematical modeling final project\
\
Currently, aircraft queuing is done via the first-come-first-serve method.\
The problem was to try and create a simple algorithm that could be used to assign aircraft a priority value to help ramp/ground controllers get the most 'valuable' aircaft airborne.\
The cost function used here takes into account the total distance/flight time to the destination of each flight, the number of passengers aboard, and how many of those passengers have connecting flights to catch.\
This is a simple demonstration of this algorithm for a small airport that has only one active runway at a time and can queue only up to 10 aircraft.

The Aircraft class is representative of each aircraft at the airport and the data available to make a decision.\
the Airport class is representative of a simple airport.\
The FlightScheduler class is responsible for generating Aircraft and flights with randomized data. This might be equivilant to a Ramp controller (in the U.S. where Ground/Ramp are seperate entities).\
The TakeoffScheduler class is responsible for letting aircraft pushback from the gate and join the queue for takeoff. This may be thought of as the Ground controller.\
The TakeoffController class is responsible for actually getting aircraft off the ground. This is may be thought of as the Tower.\

Shortcomings of this demonstration:
- it does not take into account aircraft classifications (super, heavy, etc)
- it does not take into account inbound flights
- it does not take into account ground traffic (other aircraft and ground vehicles)
- it does not take into account the topology of any specific airport
- it does not have a realistic schedule (it generates flights that need to take off faster than they can take off - always)

Yes, this was cobbled together over a weekend.
