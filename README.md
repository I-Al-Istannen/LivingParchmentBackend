## About

This is the third iteration of the small library helper I wrote.
It is intended for my personal use,
though I doubt it will *actually* be used.
We are just way too unorganized :)

## Features

This component provides the backend the android app communicates with.

It supports:
* Deleting books
* Adding books by ISBN (the backend will look them up on Goodreads or Amazon)
* Adding books by just posting the whole book you want to have at the end
* Patching books (i.e. adding some fields)
* Placing books at given locations.
  Locations are user defined and consist of a name and a description (and a uuid).
  The locations can be specified up front,
  together with the ISBN,
  or later on via the patch endpoint.
* Managing Locations is possible via the DELETE, PATCH and ADD endpoints provided.
* Querying books by a few fields (title, isbn, author, …)
  and via a few fetch modes (like partial matching, regex or exact matches)
* User accounts (though all have write permissions) and a login system

## Authentication
All endpoints except for `login` are "secured" by a JWT you need to have.
This JWT has a quite long TTL but that is totally fine for this usecase:
Nobody will delete user accounts.
User passwords are stored as `BCrypt` hashes.

## Data storage
All books, locations and users are stored in a `PostgreSql` database
which is *more* than equipped to handle the load it will get…

The storage layer is isolated behind a few repository interfaces though,
so changing and adapting that should be possible
(though not awsome. The data layer never is).

The query system could maybe benefit from a more OOP approach.
This could just be a `Query` interface with different concrete implementations
and the repo or something as the factory.
I am however not sure that will greatly help maintaining or extending it, 
so I am currently not sure it warrants the effort.

## Server software
The server runs Dropwizard and uses JDBI for database interaction.

For serialization `Gson` and `Jackson` are *both* used.
Making both parts run Jackson should just be a change in the common lib part though,
so I don't quite care about that yet.

Depenencies are managed via Google's `Dagger 2` which allows for quite easy dependency injection.
The biggest driver for decoupling resources enough to make automated unit tests possible
while not going insane wiring things together inside the application.
As a consequence the Dagger config is quite minimalistic,
uses one scope
and was certainly not the target of very in-depth tweaking.
