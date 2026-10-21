A server side mod to change the default components of items using a command.
# usage
## set
sets the default components of an item
```
/defaultcomponent set <item with components>
```
#### example
make dirt stack to 16
```
/defaultcomponent set minecraft:dirt[minecraft:max_stack_size:16]
```
make mace max durability 5:
```
/defaultcomponent set minecraft:mace[max_damage=5]
```
## reset
resets the default components of an item
```
/defaultcomponent reset <item>
```
#### example
reset components of dirt
```
/defaultcomponent reset minecraft:dirt
```
## get
gets the component changes of an item
```
/defaultcomponent get <item> 
```
gets all current changes
```
/defaultcomponent getall
```
#### example:
gets component changes of dirt
```
/defaultcomponent get minecraft:dirt
```
## in-game help message:
```
/defaultcomponent help
```