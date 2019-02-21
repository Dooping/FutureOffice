# FutureOffice
## Mobile and Ubiquitous Computing Final Project

A home office system that consists of a mobile app, server, and arduino device.

### Arduino
- Measures temperature
- Measures light
- Detects intrusions
- Controls a motor to open and close blinds (replaced by LED's since the motor was broken)
- Communication is done by wireless transmitter
- Communication between the arduino and the wireless transmitter is prone to errors so messages are sent several times and the results are averaged

### Mobile App
- Gets events from google calendar
- Allows you to pick your office location
- Uses Google Maps API to calculate how much time to get to the office
- In case you are late for a meeting allows you to send an email to evryone in the event, filling the text with the estimated time of arrival

### Server
- Relays the messages from arduino to the app and vice-versa
