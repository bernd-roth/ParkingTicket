# ParkingTicket
## Authors
[Bernd Roth](https://github.com/bernd-roth/ParkingTicket)

[Email](berndroth0@gmail.com)

## Purpose
Sending and therefore booking parking tickets in Austria.

### What is implemented
1. Automatic repeated sending of SMS to book parking tickets
   Parking ticket will be calculated for the next 24 hours, beginning with the first planned
   parking ticket.
2. Voice message if no parking ticket was booked
3. Voice message when the booked parking ticket will be outdated

### What is not implemented yet
1. Overview of parking zones in Austria
2. Overview of booked parking tickets
3. Overview of parksheriffs, marked by users
4. AI when parking ticket should be booked according to a parksheriff`s location
5. Mobile phone must vibrate if SMS was received
6. Automatic stopping of parktickets; currently this functionality is only available for Vienna

### Steps to take of how to use the app for Vienna
1.  Under menu/settings type the parkingticket`s telephone number (06646606000, ...)
2.  Your license plate
3.  Number of minutes of how long to wait for an incoming SMS until a voice message is going to tell
    you that no parking ticket was booked
4.  Which parking type should be used (15/30, 30/15 or no alternate booking)
5.  Point to the main menu and choose the time to start the first booking
    
    Example: 
        Settings:   License plate:      T-XYZ
                    Telephone number:   06646606000
                    Alternate booking:  15/30
    
        Main menu:  Time:               15:17 !Always choose 2 minutes after 00, 15, 30, 45 minutes!
                    EndTime:            depends on your likening
    
                    Intervall, City, Duration, License Plate, Phone number will not be considered,
                    since alternate booking is only implemented for Vienna and everything will be
                    overriden.
                    They will be taken from your input in settings.

### Steps to take of how to use the app for Vienna and a one-time booking
1.  Under menu/settings type the parkingticket`s telephone number (06646606000, ...)
2.  Your license plate
3.  Number of minutes of how long to wait for an incoming SMS until a voice message is going to tell
    you that no parking ticket was booked
4.  Which parking type should be used: no alternate booking
5.  Point to the main menu and choose the time to start the first booking

    Example:
        Settings:   
            License plate:      T-XYZ
            Telephone number:   06646606000
            Alternate booking:  no alternate booking

            Main menu:  Time:       15:17 !Always choose 2 minutes after 00, 15, 30, 45 minutes!
                        EndTime:    depends on your likening

### Steps to take of how to use the app for every city that needs an additional STOP signal
1.  Under menu/settings Under menu/settings type the parkingticket`s telephone number (06646606000, ...)
2.  Your license plate
3.  Number of minutes of how long to wait for an incoming SMS until a voice message is going to tell
    you that no parking ticket was booked
4.  Which parking type should be used (15/30, 30/15 or no alternate booking) - all of them will be
    ignored, if city is not Vienna
5.  Point to the main menu and choose the time to start the first booking

    Example:
        Settings:   License plate:      T-XYZ
                    Telephone number:   06646606000
                    Alternate booking:  No alternate booking available
        Intervall:  Since Villach needs an additional STOP Signal to stop booking,
                    you have to choose an interval that exceeds the duration parkingticket!

        Main menu:  Time:               15:17 !Always choose 2 minutes after 00, 15, 30, 45 minutes!

### Support
If you have questions or if you want further implementations,
please write me an Email: berndroth0@gmail.com