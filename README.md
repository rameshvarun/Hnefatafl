# Hnefatafl

## Programming Patterns
### Retrolabmda
Right now, we're using Retrolambda, which lets you use Java 8 lambda syntax. Essentially, this works in any situation in which you would create an anonymous class from an single-function interface.
```java
builder.setTitle("You Have Won The Game.")
  .setPositiveButton("Return to Menu", (DialogInterface sideDialog, int which) -> {
    NavUtils.navigateUpFromSameTask(this);
  }).show();
```
### Stream API Backport
We're also using a backport of the Java 8 Stream API, which lets you do stuff like `map`, `filter`, `all`, etc. Perfect for use with Retrolambda.
```java
return Stream.of(Direction.values()).allMatch((Direction dir) -> {
  // The King is only captured when surrounded on all four sides.
  Position adjacent = defendingPos.getNeighbor(dir);
  return pieces.containsKey(adjacent) && pieces.get(adjacent).hostileTo(piece);
});
```

### Other
- Try to use `final` wherever it makes sense.

## Packages
- [Simulator](./app/src/main/java/net/varunramesh/hnefatafl/simulator) - The simulator handles all of the rules of Hnefatafl, exporting the `Board`class that represents the state of a game.
- [Hnefatafl AI](./app/src/main/java/net/varunramesh/hnefatafl/ai) - This package implements various strategies for controlling the AI player in Player vs. AI games.
- [The Game](./app/src/main/java/net/varunramesh/hnefatafl/game) - This package handles all of the drawing / input, using LibGDX.

## Assets Used
- "Normans Bayeux" by Dan Koehl - Tapestry de Bayeux. Licensed under CC BY-SA 3.0 via Wikimedia Commons - https://commons.wikimedia.org/wiki/File:Normans_Bayeux.jpg#/media/File:Normans_Bayeux.jpg
- "Norse free font" by oël Carrouché. Licensed under Free font EULA v1.0
- "Material icons" by Google - Licensed under CC BY 4.0
