# AirportQueuingProblem
A Java implementation of a cost function for NJIT MATH 227 Final project\
\
Currently, aircraft queuing is done via the first-come-first-serve method.\
The problem was to try and create a simple algorithm that could be used to assign aircraft a priority value to help ramp/ground controllers get the most 'valuable' aircaft airborne.\
The cost function used here takes into account the total distance/flight time to the destination of each flight, the number of passengers aboard, and how many of those passengers have connecting flights to catch.\
This is a simple demonstration of this algorithm for a small airport that has only one active runway at a time and can queue only up to 10 aircraft.

Yes, this was cobbled together over a weekend.
