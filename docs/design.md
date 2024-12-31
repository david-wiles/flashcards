# DESIGN

## Introduction

This document describes the design of the project. It is intended to provide a high-level overview of the project's 
architecture and design decisions.

Many features which I would like to implement have been left out due to cost constraints. For example, the app does not
currently support multi-factor authentication (MFA) since it would require a paid service to send SMS messages or emails.

## Architecture

The project is built with a classic client-server architecture. The client is a single-page web app built with Scala.js
and the backend is a RESTful API built with Tapir. The client communicates with the server via HTTP requests. Flashcards,
users, and other data is stored in a PostgreSQL database. Large content such as images, audio, and video are stored on 
disk and referenced in the database. (This decision was made to keep the project low-cost; a more scalable solution would
be to store these files in a cloud storage service such as AWS S3.)

## Components

### User authentication

Users can create an account and log in to the app. User authentication is handled with JWT tokens. All requests to the
API must include a valid JWT token in the `Authorization` header. 

Users are stored in the database and may only be authenticated by password. Passwords are hashed with bcrypt and salted 
before being stored in the database. MFA is not yet implemented. 

### Admin page

An admin page is necessary for the app to manage users and flashcards. The admin page will be accessible only to
administrators (me) to ensure that misuse of the app is minimized. This page will allow the administrator to view or 
delete any flashcards and users. Additionally, new users must be approved by an administrator before they can
use the app.

### Flashcards

Flashcards are stored in the database and are accessed via the RESTful API. 

### Spaced Repetition

Spaced repetition algorithm is implemented on the server using a modified version of the
[Leitner system](https://en.wikipedia.org/wiki/Leitner_system). The Leitner system is a simple spaced repetition, but 
it requires that all flashcards are reviewed during each session. Anticipated usage of this app is that users will
review flashcards sporadically and there may be hundreds or thousands of flashcards to review. To accommodate this, the
algorithm is modified to continuously update the schedule for each flashcard as the user reviews them. 

The system uses a five-box approach to scheduling flashcards. When a flashcard is created, it is placed in box 1. If the
user recalls the answer correctly, the flashcard is moved to the next box. If the user recalls the answer incorrectly or 
takes too long to answer, the flashcard is moved back to the previous box. The time between reviews increases as the 
flashcard moves to higher boxes. Flashcards in each box are treated as a queue and are reviewed in order, and the next
box to pull a flashcard from is randomly determined with the probability of selecting a box decreasing as the box number
increases. If there are no flashcards in a box then a flashcard is pulled from the next box.

Implementation of the algorithm's state is done using a separate table with a many-to-1 relationship with the flashcards
table. Each row in the table represents a flashcard's state for a specific user. The table contains the box number and 
the last review time for each flashcard. This table is indexed by user, box number, and review time to allow for 
efficient retrieval of flashcards. When a user reviews a flashcard, the server updates the box number and review time 
for that flashcard in the table. If the review session is based on a specific category, the server must only return
flashcards from that category which will require a join on the flashcards table to filter by category.


### Search

The user can search for flashcards by keyword. The search is implemented on the server and returns a list of flashcards
that match the keyword. Ideally, the search would be implemented with a full-text search engine such as Elasticsearch,
but to keep the app simple the search is currently implemented using PostgreSQL's built-in full-text search capabilities.
