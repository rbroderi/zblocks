Import-Module EPS
Set-StrictMode -Version 2.0
$colors=$null
$color=$null
$seperator=$null
$colors = ("base","red","blue","green","cyan","magenta","yellow")
foreach($color in $colors){
if($color -eq "base"){
$seperator = ""
$color=""
}
else{
$seperator = "_"
}
Invoke-EpsTemplate -Path $PSScriptRoot\push_block.eposh | Out-File -Encoding ascii "$PSScriptRoot\push_block$seperator$color.json"
Invoke-EpsTemplate -Path $PSScriptRoot\depress_block.eposh | Out-File -Encoding ascii "$PSScriptRoot\depress_block$seperator$color.json"
}

