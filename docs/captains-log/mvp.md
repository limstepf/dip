Model-View-Presenter (Passive View)
===================================

The Passive View Pattern - "yet another variation on model-view-controller and model-view-presenter" - is a perfect match for JavaFX, since not much convention is needed, and because the pattern can grow as needed. 
The model is dumb (doesn't know about the presenter, or the view) and completely managed by the presenter. And so is the view (hence called passive). Presenters can be easily stacked as needed, s.t. a parent presenter manages a set of child presenters.

```text

  +-------+     +-------------------------+
  | Model |     |        View             |
  +-------+     +-------------------------+
      ^         | + Parent getComponent() |
      |         +-------------------------+
      |                   ^
      |                   |
      |                   |
   +-------------------------+
   |       Presenter         |
   +-------------------------+
   | + Parent getComponent() |
   +-------------------------+

```

In the simplest case there's just the presenter with a hardcoded/merged view (returned by a call to `getComponent()`), that can be later refactored into an view-interface and multiple implemenations to support any views (e.g. a mock view for testing) as needed. 
There's also no need for magic dependency injection framework and tricks: just pass components around the old fashioned way by the constructor.


Further reading:
* [http://martinfowler.com/eaaDev/PassiveScreen.html](http://martinfowler.com/eaaDev/PassiveScreen.html)
* [http://martinfowler.com/articles/injection.html](http://martinfowler.com/articles/injection.html)



FXML and the JavaFX Scene Builder
---------------------------------
JavaFX Scene Builder is a visual layout tool that lets users quickly design JavaFX application user interfaces, without coding. Also it sucks. Hard. And you know what doesn't? Coding.

FXML isn't helpful at all (not even to speak of the wonky FXML produced by the Scene Builder...), and needlessly complicates things by enforcing a more classic MVC pattern, combined with magic injection tricks and other sorts of madness. Just no.
