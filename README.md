##### Project GlobalWaves - Marin Radu

# README

## View as webpage

```
sudo pip3 install grip
grip  README.md
# open http://localhost:6419/
```

# Content

1. [Overview](#overview)
2. [Description](#solution-description)
3. [App Design](#design-patterns)
4. [Additional information](#notice)

## Overview

Project GlobalWaves is an attempt to create a personalised Spotify clone from scratch.
Most of the backend has been handled here, with possible future additions to the functionalities.

All that's left is for the frontend to handle the creation and transmission of the commands as JSONs.

## Program description

Version 3.0

**New features:**

*GlobalWaves wrapped*, similarly to Spotify wrapped, can be used by any user type to view relevant statistics such as:

- for users: the top artists, albums, songs, genres and episodes they have listened to on the app
- for artists: the top albums, songs, fans and the number of listeners
- for hosts: the top podcasts and the number of listeners

New classes were added to compute and store these results (see `StatResult` and `ArtistRevenue`).

*Monetization* was implemented. For this purpose, premium users and ads for non-premium users were added, 
as well as a buy system for merch. The final monetization distribution is shown for future reviews and business 
planning and to serve as incentive for artist collaborations.

Keeping track of the money and user activity was done by introducing `listens`. This basically means that
each user stores all the listened songs for future reference, as well as the untracked listened songs for
applying the ad / premium costs. Everything statistics-related was done using `HashMaps` / `LinkedHashMaps`, 
usually storing a name and a listen count. A "global" map was also used to store song revenues. 
This was necessary in finding the most profitable song of each artist.

The listened songs / episodes maps are also used for GlobalWaves wrapped, since they contain all the information on the
activity of the user. Two maps were used for the listen history.

The addition of listens to audio files is done by the file object itself, since this action is complementary to simulateTime.
Ad time was taken into account by the media player. Loading a file cancels out the ad. Money from ads is received 
when the ad starts playing, as instructed.

*Page navigation* was extended by adding the possibility of changing the current page to the currently playing artist's / host's page.
A *subscription system* was also implemented. Users subscribed to a content creator's page will receive notifications on
newly-added merch, albums, podcasts etc. and store them in a list until deciding to view them.

Each user now has a page history (a list) that can be navigated and updated by using changePage.

Premium users cannot be deleted. A tie is automatically added when buying premium and removed when canceling to implement this.

*Recommendations* for users have been added. They can request either a random song to be added to their recommended song list, or
a random / fans' playlist. They can also load the last recommended audio file. Recommendations are mostly genre-related and are
generated through the same process as the previously mentioned statistics. The last generated file is stored in the user class.

### Design patterns

Several design patterns were used for future extensions, readability and logical flow:

1. **Singleton**: used on databases, since they only need one instance
2. **Visitor**: used for computing statistics, since the object storing them has common attributes for the different
user types
3. **Factory**: used for creating responses, since the response type is uncertain when starting the execution of a command
4. **Abstract Factory**: used for choosing a factory type, for the same reason as above
5. **Observer**: used for `notifying` users of changes on a subscribed page, since that is what the pattern was originally defined for

Other influences:

- Command: changed into a "response pattern", since responses set their data and create the response node themselves
- Template Method: some methods, such as `printPage`, call `protected` functions for future inheritors to modify. printPage only
contains the skeleton of the implementation
- Flyweight: most data is only created once, to be stored by multiple entities of different types (ex: the audio files)

### Notice

More details can be found inside the Javadoc comments and any additional comments left in the code, as well as through variable and method names.
Also visit [this link](https://ocw.cs.pub.ro/courses/poo-ca-cd/teme/proiect/etapa3) for further documentation on the implementation and restrictions.

**Note** that the code currently misses a build.gradle or Dockerfile. Jackson and Lombok should be added as dependencies in the future.

**Also note** that SDK-19 was used. Please check for future deprecation or upgrades.
