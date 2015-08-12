# The Game

The actual game screen is implemented using LibGDX. PlayerActivity is the actual activity, which contains a frame layout (enables a stack of views). The bottom of the stack is a SurfaceView managed by LibGDX. Other views are put on top of it for UI elements.
