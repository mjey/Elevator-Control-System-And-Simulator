# Use Cases
![Use Case](https://github.com/Mjeyz/Elevator-Control-System-And-Simulator/raw/main/Assets/Images/Cases.JPG)
## Select Destination Use Case
**Actors:** Elevator User (primary), Arrival Sensor<br>
**Precondition:** User is in the elevator
### Description
* User presses an elevator floor button. The elevator button sensor sends the elevator button request to the system, identifying the destination floor the user wishes to visit.
* The new request is added to the list of floors to visit. If the elevator is stationary; the system  determines in which direction the system should move in order to service the next request. The system commands the elevator door to close. When the door has closed, the system commands the motor to start moving the elevator, either up or down.
* As the elevator moves between floors, the arrival sensor detects that the elevator is approaching a floor and notifies the system. The system checks whether the elevator should stop at this floor. If so, the system commands the motor to stop. When the elevator has stopped, the system commands the elevator door to open.
* If there are other outstanding requests, the elevator visits these floors on the way to the floor requested by the user. Eventually, the elevator arrives at the destination floor selected by the user.
### Alternatives:
• If the elevator is at a floor and there is no new floor to move to, the elevator stays at the current floor, with the door open.
> **Postcondition**: Elevator has arrived at the destination floor selected by the user
## Request Elevator Use Case
**Actors**: Elevator User (primary), Arrival Sensor<br>
**Precondition**: User is at a floor and wants an elevator.
### Description:
1. User presses the up floor button. The floor button sensor sends the user request to the system, 
identifying the floor number.
2. The system selects an elevator to visit this floor. The new request is added to the list of floors to visit. If the elevator is stationary, the system determines in which direction the system should move in order to service the next request.The system commands the elevator door to close. After the door has closed, 
the system commands the motor to start moving the elevator, either up or down.
3. As the elevator moves between floors, the arrival sensor detects that the elevator is approaching a floor and notifies the system. The system checks whether the elevator should stop at this floor, if so, the system commands the motor to stop. When the elevator has stopped, the system commands the elevator door to open.
4. If there are other outstanding requests, the elevator visits these floors on the way to the floor requested by the user. Eventually, the elevator arrives at the floor in response to the user request.
### Alternatives: 
• User presses the down floor button to move down. System response is the same as for the main sequence.
• If the elevator is at a floor and there is no new floor to move to, the elevator stays at the current floor, with the door open.
> **Postcondition**: Elevator has arrived at the floor in response to user request.
## Static Model of the Problem Domain
Each elevator has:
* **A set of elevator buttons:** The user presses one to select a floor.
* **A set of elevator lamps:** The lamps indicate the floor(s) which will be visited by the elevator.
* **An elevator motor:** The motor moves the elevator between floors.
* **An elevator door:** The elevator door also opens and closes the floor doors.
![Static Model of the Problem Domain](https://github.com/Mjeyz/Elevator-Control-System-And-Simulator/raw/main/Assets/Images/Static%20Model.JPG)
<br>Each floor has:
* **Up and down floor buttons:** The user presses a button to request an elevator. Note that the top and bottom 
floors will only have one button.
* **Floor lamps:** The lamps indicated which buttons have been pushed.
* **Direction lamps:** Each elevator has a set of lamps to denote the arrival and direction of an elevator at a 
floor.
* **Arrival sensors:** Each elevator shaft at each floor has a sensor to detect the presence of an elevator
