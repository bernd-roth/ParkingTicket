# ParkingTicket
## Authors
[Bernd Roth](https://github.com/bernd-roth/ParkingTicket)

## Purpose
Sending and therefore booking parking tickets in Austria.

### What is implemented
1. Automatic repeated sending of SMS to book parking tickets
2. Voice message if no parking ticket was booked
3. Voice message when the booked parking ticket will be outdated

#### What to implement in future
1. Overview of parking zones in Austria
2. Overview of booked parking tickets
3. Overview of parksheriffs, marked by users
4. AI when parkingticket should be booked according to a parksheriff`s location
5. Mobile phone must vibrate if SMS was received

##### Steps to take of how to use the app
1.  Under menu/settings choose your current location for your car
2.  Type in your telephone number where the SMS should be sent to (06646606000, ...)
3.  Your license plate
4.  Number of minutes of how long to wait for an incoming SMS until a voice message is going to tell
    you that no parking ticket was booked
5.  Which parking type should be used (15/30, 30/15 or no alternate booking)
    If you current location is Vienna (Wien), then alternate booking will be considered
    So, intervall in main menu will be overriden