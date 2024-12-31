# FLASHCARDS

A simple web-only flashcard app for memorization.

[Spaced repetition](https://en.wikipedia.org/wiki/Spaced_repetition) is a learning technique that incorporates 
increasing intervals of time between subsequent review of previously learned material in order to exploit the 
psychological spacing effect.

This app is strictly for fun, I was inspired by the simplicity of the technique and wanted to build my own app to 
aid in memorization. 

## Features

1. Create flashcards. Flashcards may contain text, images, audio, and video. They are associated with some kind of 
    question and answer. Each flashcard may be tagged with one or more categories to help with organization and 
    enable review of flashcards by category.
2. Review flashcards. The app will present flashcards to the user in a spaced repetition pattern. The user will 
    be asked to recall the answer to the question on the flashcard. The app will then schedule the next review based on
    whether the response was correct and the time taken to respond.
3. Search for flashcards. The user can search for flashcards by keyword.
4. Edit flashcards. The user can edit the question, answer, and content of a flashcard.
5. Delete flashcards. The user can delete flashcards.
6. Share flashcards. The user can share flashcards with other users.

### Meta-Features 

1. User authentication. Users can create an account and log in to the app.
2. User authorization. Users can only view, edit, and delete their own flashcards. 
3. Flashcard sharing. Users can share flashcards with specific users for either viewing or editing. 
4. Flashcard statistics. Users can view statistics about their flashcards, such as the number of flashcards they have 
    created, the number of flashcards they have reviewed, and the number of flashcards they have shared.

## Technologies

This app is built entirely with Scala. The frontend is built with Scala.js and the backend is built with Tapir.

## Documentation

The API documentation is available at `/docs`. Design of the project is located in [design.md](./docs/design.md). 