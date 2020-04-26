# zblocks
Pushable Puzzle Blocks Mod for minecraft

Updated release of Pushable Puzzle Blocks

This release includes a custom modeled blocks that can be used in adventure maps.

PushPuzzleBlock:
	This block can be pushed by standing and facing next to it and left clicking, it will slide one block in that direction.
	This block is gravity enabled so it will fall when pushed off of things.

DepressPuzzleBlock:
	Restone emitting block; detects if a matching PushPuzzleBlock is in space above it. Like colors match, the uncolored varient will match with any color
ActivatePuzzleBlock:
	Activates on hit with arrow or left click by player
	Activates all TransientPuzzleBlocks in 100x50x100 area around it.

TransientPuzzleBlock:
	When deactivated this block is not solid and can be passed through like air.
	When activated this is a solid block
Hourglass:
	Resets All PushPuzzle blocks back to their initial placement in 100x100x100 area around block
	(reset still has some bugs)

StartPuzzleBlock:
	Decrotive block that can be used to denote initial PushPuzzleBlock locations


Blocks have custome sounds as well.

This release is for 1.12.2


