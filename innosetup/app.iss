[Setup]
AppName=Inferno Launcher
AppPublisher=Inferno
UninstallDisplayName=Inferno
AppVersion=${project.version}
AppSupportURL=https://discord.gg/eFvvHN4hBd
DefaultDirName={localappdata}\Inferno

; ~30 mb for the repo the launcher downloads
ExtraDiskSpaceRequired=30000000
ArchitecturesAllowed=x64
PrivilegesRequired=lowest

WizardSmallImageFile=${basedir}/app_small.bmp
WizardImageFile=${basedir}/left.bmp
SetupIconFile=${basedir}/app.ico
UninstallDisplayIcon={app}\Inferno.exe

Compression=lzma2
SolidCompression=yes

OutputDir=${basedir}
OutputBaseFilename=InfernoSetup64

[Tasks]
Name: DesktopIcon; Description: "Create a &desktop icon";

[Files]
Source: "${basedir}\app.ico"; DestDir: "{app}"
Source: "${basedir}\left.bmp"; DestDir: "{app}"
Source: "${basedir}\app_small.bmp"; DestDir: "{app}"
Source: "${basedir}\native-win64\Inferno.exe"; DestDir: "{app}"
Source: "${basedir}\native-win64\Inferno.jar"; DestDir: "{app}"
Source: "${basedir}\native\build64\Release\launcher_amd64.dll"; DestDir: "{app}"
Source: "${basedir}\native-win64\config.json"; DestDir: "{app}"
Source: "${basedir}\native-win64\jre\*"; DestDir: "{app}\jre"; Flags: recursesubdirs

[Icons]
; start menu
Name: "{userprograms}\Inferno"; Filename: "{app}\Inferno.exe"
Name: "{userdesktop}\Inferno"; Filename: "{app}\Inferno.exe"; Tasks: DesktopIcon

[Run]
Filename: "{app}\Inferno.exe"; Parameters: "--postinstall"; Flags: nowait
Filename: "{app}\Inferno.exe"; Description: "&Open Inferno"; Flags: postinstall skipifsilent nowait

[InstallDelete]
; Delete the old jvm so it doesn't try to load old stuff with the new vm and crash
Type: filesandordirs; Name: "{app}\jre"

[UninstallDelete]
Type: filesandordirs; Name: "{%USERPROFILE}\.inferno\repository2"
; includes install_id, settings, etc
Type: filesandordirs; Name: "{app}"

[Code]
#include "upgrade.pas"