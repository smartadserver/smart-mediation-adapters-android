# Smart Mediation Adapters Android - Vungle

## Known issue

### Support for only one Vungle placement
Due to __Vungle__ SDK way to load and precache placements, you will be able to load only one placement per application launch. __Vungle__ must be initialized with all placements you will want to use through an application lifecycle. Mediation insertions of __Smart__ will only take one placement as input and initialize the __Vungle__ SDK with this placement, locking __Vungle__ SDK in a state where only this placement will be available until the app is killed and the __Vungle__ SDK re-activated.
