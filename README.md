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
4. Overview of parkingtickets that must be booked

### What is not implemented yet
1. Overview of parking zones in Austria
2. Overview of parksheriffs, marked by users
3. AI when parking ticket should be booked according to a parksheriff`s location
4. Mobile phone must vibrate if SMS was received
5. Automatic stopping of parking tickets; currently this functionality is only available for Vienna

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
        Interval:   Since Villach needs an additional STOP Signal to stop booking,
                    you have to choose an interval that exceeds the duration parkingticket!

        Main menu:  Time:               15:17 !Always choose 2 minutes after 00, 15, 30, 45 minutes!

### What happens if no answer of a successful booked parking ticket was received?
Currently, the booking procedure will be stopped automatically.
That means, if no answer is received within a defined time (this time will be set by yourself),
the booking procedure will be stopped automatically.
        
### Pictures
![Screenshot_20220412-122236](https://user-images.githubusercontent.com/1835491/162941274-1c99cffc-852b-4b65-9f60-c0b56a415dc3.png)
![Screenshot_20220412-122247](https://user-images.githubusercontent.com/1835491/162941359-db0ae893-da4b-4a80-98dd-ac70721aad9f.png)
![Screenshot_20220412-122305](https://user-images.githubusercontent.com/1835491/162941363-b3f62eae-8951-4242-affa-d0594ca47fc0.png)
![Screenshot_20220412-122355](https://user-images.githubusercontent.com/1835491/162941364-07133d70-fd8f-433a-aa65-b607d3a4d300.png)
![Screenshot_20220412-122403](https://user-images.githubusercontent.com/1835491/162941366-9ac8f84d-0bf5-44d3-85bd-00973d1c8097.png)
![Screenshot_20220723-230609](https://user-images.githubusercontent.com/1835491/180623086-8864cb01-a679-4bcb-a779-c6bd2c529a5e.png)
![Screenshot_20220722-232649](https://user-images.githubusercontent.com/1835491/180571490-332f09b4-a690-4bae-a3bb-5506c6546e85.png)
![Screenshot_20220722-232639](https://user-images.githubusercontent.com/1835491/180571508-e6a2b4b1-640e-4e9e-9b36-f6e0873a919f.png)
![Screenshot_20220723-230814](https://user-images.githubusercontent.com/1835491/180623089-23a078b0-e91c-4ebc-80e9-e1d31a44f509.png)

### Support
If you have questions or if you want further implementations,
please write me an Email: berndroth0@gmail.com
