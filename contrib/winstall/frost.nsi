# Frost Windows Installer
# Copyright (C) 2001 Benoit Laniel <nels@pgroupe.net>
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

# Used strings:
# $1 : javaw.exe path
# $2 : Installed flag ('Yes' if Frost was already installed, otherwise 'No')
# $3 : java.exe path

Name "Frost ${CONF_VERSION}"
OutFile "${LANG}.exe"
Icon jtc_inst.ico
EnabledBitmap yes.bmp
DisabledBitmap no.bmp

InstProgressFlags smooth

LicenseData "data\frost\docs\gpl.txt"

UninstallExeName Uninstall-Frost.exe

InstProgressFlags smooth
# !packhdr will further optimize your installer package if you have upx.exe in your directory
!packhdr temp.dat "upx.exe --best temp.dat"

InstallDir "$PROGRAMFILES\Frost"
InstallDirRegKey HKEY_LOCAL_MACHINE "Software\Frost" "InstPath"

;-----------------------------------------------------------------------------------
Section
  # Required section which copies all the files

  # Have to say to the installer that Frost is 685 Kb since we only copy files
  AddSize ${CONF_SIZE}

  # -- Assume Frost is already installed
  StrCpy $2 "Yes"
  # -- Check if frost.jar exists
  IfFileExists "$INSTDIR\frost.jar" DoUpdate
  StrCpy $2 "No"

DoUpdate:
  DetailPrint "${DET_COPYING}"
  SetOutPath "$INSTDIR"
  CopyFiles "$EXEDIR\frost\*.*" "$INSTDIR"

  # Register installpath
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Frost" "InstPath" '$INSTDIR'

  # Create uninstall keys
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\Frost" "DisplayName" "Frost ${CONF_VERSION}"
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\Frost" "UninstallString" '"$INSTDIR\Uninstall-Frost.exe"'
SectionEnd

;-----------------------------------------------------------------------------------
Section "${SEC_ICONS}"
  # This section creates the startmenu icons
  SectionIn 1,2

  CreateDirectory "$SMPROGRAMS\Frost"
  CreateShortCut "$SMPROGRAMS\Frost\Frost.lnk" "$1" "-cp frost.jar frost" "$INSTDIR\jtc.ico" 0
  CreateShortCut "$SMPROGRAMS\Frost\${LNK_FROSTCONSOLE}.lnk" "$3" "-cp frost.jar frost" "$INSTDIR\jtc.ico" 0
  CreateShortCut "$SMPROGRAMS\Frost\${LNK_UNINSTALL}.lnk" "$INSTDIR\Uninstall-Frost.exe" "" "$INSTDIR\Uninstall-Frost.exe" 0
  WriteINIStr "$SMPROGRAMS\Frost\${LNK_HOMEPAGE}.url" "InternetShortcut" "URL" "http://jtcfrost.sourceforge.net"
SectionEnd

;-----------------------------------------------------------------------------------
Section "${SEC_DESKTOPICON}"
  # This section creates a desktop icon
  SectionIn 1,2

  CreateShortCut "$DESKTOP\Frost.lnk" "$1" "-cp .;classes;data frost" "$INSTDIR\jtc.ico" 0
SectionEnd

;-----------------------------------------------------------------------------------
Section "Uninstall"
  # Uninstall section
  RMDir /r "$INSTDIR"
  Delete "$DESKTOP\Frost.lnk"
  RMDir /r "$SMPROGRAMS\Frost"

  DeleteRegKey HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\Frost"
  DeleteRegKey HKEY_LOCAL_MACHINE "Software\Frost"
SectionEnd

;-----------------------------------------------------------------------------------
Function .onInit
  # Check if Freenet is installed
  ReadRegStr $0 HKEY_LOCAL_MACHINE "Software\Freenet" "instpath"
  StrCmp $0 "" NoFreenet

  # Yes, then get the java path ;)
  ReadINIStr $4 "$0\FLaunch.ini" "Freenet Launcher" "javaexec"
  # Get the short path name
  GetFullPathName /SHORT $3 $4

  ReadINIStr $4 "$0\FLaunch.ini" "Freenet Launcher" "javaw"
  # Get the short path name
  GetFullPathName /SHORT $1 $4
  Goto End

NoFreenet:
  # Try to find it
  # Search a java.exe file
  SearchPath $0 "java.exe"
  StrCmp $0 "" NoJava
  GetFullPathName /SHORT $3 $0

  # Search a javaw.exe file
  SearchPath $0 "javaw.exe"
  StrCmp $0 "" NoJava
  GetFullPathName /SHORT $1 $0
  Goto End

NoJava:
  # No, then abort installation
  MessageBox MB_OK|MB_ICONSTOP "${STR_JAVANOTFOUND}"
  Abort

End:  
FunctionEnd

;-----------------------------------------------------------------------------------
Function .onInstFailed
  # -- Do not delete INSTDIR if frost.class was there before installation
  StrCmp $2 "Yes" DontDelete
  RMDir /r "$INSTDIR"

DontDelete:
FunctionEnd