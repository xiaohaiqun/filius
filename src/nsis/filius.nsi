; Script generated by the HM NIS Edit Script Wizard.

; HM NIS Edit Wizard helper defines
!define PRODUCT_NAME "Filius"
!define PRODUCT_VERSION "${version}"
!define PRODUCT_PUBLISHER "${publisher}"
!define PRODUCT_WEB_SITE "${url}"
!define PRODUCT_DIR_REGKEY "Software\Microsoft\Windows\CurrentVersion\App Paths\Filius.exe"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"

; MUI 1.67 compatible ------
!include "MUI.nsh"

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"

; Language Selection Dialog Settings
!define MUI_LANGDLL_REGISTRY_ROOT "${PRODUCT_UNINST_ROOT_KEY}"
!define MUI_LANGDLL_REGISTRY_KEY "${PRODUCT_UNINST_KEY}"
!define MUI_LANGDLL_REGISTRY_VALUENAME "NSIS:Language"

; Welcome page
!insertmacro MUI_PAGE_WELCOME
; License page
!insertmacro MUI_PAGE_LICENSE "GPLv3.txt"
!insertmacro MUI_PAGE_DIRECTORY
; Instfiles page
!insertmacro MUI_PAGE_INSTFILES
; Finish page
!insertmacro MUI_PAGE_FINISH

; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

; Language files
!insertmacro MUI_LANGUAGE "German"

; MUI end ------

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
OutFile "Filius-Setup-${PRODUCT_VERSION}.exe"
InstallDir "$PROGRAMFILES64\Filius"
InstallDirRegKey HKLM "${PRODUCT_DIR_REGKEY}" ""
ShowInstDetails show
ShowUnInstDetails show

; http://nsis.sourceforge.net/Refresh_shell_icons
!define SHCNE_ASSOCCHANGED 0x08000000
!define SHCNF_IDLIST 0
Function RefreshShellIcons
  ; By jerome tremblay - april 2003
  System::Call 'shell32.dll::SHChangeNotify(i, i, i, i) v \
  (${SHCNE_ASSOCCHANGED}, ${SHCNF_IDLIST}, 0, 0)'
FunctionEnd

Function .onInit
  !insertmacro MUI_LANGDLL_DISPLAY
FunctionEnd

Section "Filius" SEC01
  AddSize 6700
  SetShellVarContext all

  SetOutPath "$INSTDIR"
  SetOverwrite try
  File "Changelog.txt"
  SetOutPath "$INSTDIR"
  File "Einfuehrung_Filius.pdf"
  CreateDirectory "$SMPROGRAMS\Filius"
  CreateShortCut "$SMPROGRAMS\Filius\Filius.pdf.lnk" "$INSTDIR\Einfuehrung_Filius.pdf" 
  File "Filius.exe"
  CreateShortCut "$SMPROGRAMS\Filius\Filius.lnk" "$INSTDIR\Filius.exe"
  File "filius.jar"
  File "Filius.sh"
  File "Filius.command"
  File "GPLv2.txt"
  File "GPLv3.txt"
  File /r "classes\config"
  File /r "classes\hilfe"
  File /r "classes\img"
  File /r "lib"
  File /r "classes\tmpl"
  ; Dateityp '.fls' zuordnen
  WriteRegStr HKLM "SOFTWARE\Classes\.fls\shell\open\command" "" '"$INSTDIR\Filius.exe" "%1"'
  WriteRegStr HKLM "SOFTWARE\Classes\.fls\DefaultIcon" "" "$INSTDIR\Filius.exe"
  Call RefreshShellIcons
SectionEnd

Section -AdditionalIcons
  SetOutPath $INSTDIR
  WriteIniStr "$INSTDIR\${PRODUCT_NAME}.url" "InternetShortcut" "URL" "${PRODUCT_WEB_SITE}"
  CreateShortCut "$SMPROGRAMS\Filius\www.lernsoftware-filius.de.lnk" "$INSTDIR\${PRODUCT_NAME}.url" "" "$INSTDIR\Filius.exe" 
SectionEnd

Section -Post
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr HKLM "${PRODUCT_DIR_REGKEY}" "" "$INSTDIR\Filius.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayIcon" "$INSTDIR\Filius.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
SectionEnd


Function un.onUninstSuccess
  HideWindow
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) wurde erfolgreich deinstalliert." /SD IDOK
FunctionEnd

Function un.onInit
!insertmacro MUI_UNGETLANGUAGE
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Sie sind dabei $(^Name) und alle seinen Komponenten zu entfernen. Fortfahren?" /SD IDYES IDYES +2
  Abort
FunctionEnd

Section Uninstall
  SetShellVarContext all

  Delete "$SMPROGRAMS\Filius\www.lernsoftware-filius.de.lnk"
  Delete "$SMPROGRAMS\Filius\Filius.lnk"
  Delete "$SMPROGRAMS\Filius\Filius.pdf.lnk"

  RMDir "$SMPROGRAMS\Filius"
  RMDir /r "$INSTDIR"
  RMDir ""

  DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
  DeleteRegKey HKLM "${PRODUCT_DIR_REGKEY}"
  ; Dateitypzuordnung entfernen
  DeleteRegKey HKLM "SOFTWARE\Classes\.fls"
  SetAutoClose true
SectionEnd