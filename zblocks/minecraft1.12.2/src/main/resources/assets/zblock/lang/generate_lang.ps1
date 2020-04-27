Import-Module EPS
Set-StrictMode -Version 2.0
$colors=$null
$color=$null
$seperator=$null
$colorSpace=$null
$colors = ("base","red","blue","green","cyan","magenta","yellow")

$output=""
foreach($color in $colors){
if($color -eq "base"){
$seperator = ""
$color=""
}
else{
$seperator = "_"
$colorSpace=$color+" "
}
$TextInfo = (Get-Culture).TextInfo
$output+= "tile.push_block$seperator$color.name=$($TextInfo.ToTitleCase($colorSpace))Push Puzzle Block"+ "`n"
$output+= "tile.depress_block$seperator$color.name=$($TextInfo.ToTitleCase($colorSpace))Depressible Puzzle Block" + "`n"
$output+= "tile.start_block$seperator$color.name=$($TextInfo.ToTitleCase($colorSpace))Starting Puzzle Block"+ "`n"
}
$output+= "tile.hourglass.name=Hourglass" + "`n"
$output+= "tile.transient_block.name=Transient Puzzle Block" + "`n"
$output+= "tile.secret_transient_block.name=Secret Transient Puzzle Block" + "`n"
$output+= "tile.activate_block.name=Activate Puzzle Block" + "`n"
$output.Trim() | Out-File -Encoding ASCII $PSScriptRoot\en_us.lang
