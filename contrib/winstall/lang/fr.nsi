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

# Translator : Benoit Laniel <nels@pgroupe.net>

!include config.nsh
!define LANG "fr"

# Section names
!define SEC_DESKTOPICON "Ic�ne sur le bureau"
!define SEC_ICONS "Ic�nes du menu d�marrer"

# Details strings
!define DET_COPYING "Copie des fichiers..."

# Mic strings (in messagebox...)
!define STR_JAVANOTFOUND "Frost n�cessite Java pour fonctionner, veuillez d'abord l'installer (aucun java.exe/javaw.exe n'a �t� trouv�)."

# Link strings
!define LNK_FROSTCONSOLE "Frost (console)"
!define LNK_HOMEPAGE "Page d'accueil"
!define LNK_UNINSTALL "D�sinstaller"

# Installation types
InstType /CUSTOMSTRING=Personnalis�e
InstType Minimale
InstType Compl�te

# Dialogs
LicenseText "Frost est distribu� sous la licence publique g�n�rale GNU:" "J'accepte"
ComponentText "Ceci installera Frost ${VERSION} sur votre syst�me." "S�lectionnez le type d'installation:" "ou, s�lectionnez les composants � installer:"
DirText "Aucun fichier ne sera plac� en dehors de ce dossier (c.a.d. Windows\system)"  "S�lectionnez le dossier dans lequel Frost sera install�:" "Parcourir..."
UninstallText "Ceci d�sinstallera Frost de votre syst�me" "� partir de: "

# Captions
Caption "Installation de Frost"
SubCaption 0 ": Accord de licence"
SubCaption 1 ": Options d'installation"
SubCaption 2 ": Dossier d'installation"
SubCaption 3 ": Installation des fichiers"
SubCaption 4 ": Termin�"

UninstallCaption "D�sinstallation de Frost"
UninstallSubCaption 0 ": Confirmation"
UninstallSubCaption 1 ": D�sinstallation des fichiers"
UninstallSubCaption 2 ": Termin�"

# Buttons
MiscButtonText "< Pr�c�dent" "Suivant >" "Annuler" "Fermer"
DetailsButtonText "D�tails"
InstallButtonText "Installer"
UninstallButtonText "D�sinstaller"

# Misc
SpaceTexts "Espace requis: " "Espace disponible: "
CompletedText "Termin�"

!include frost.nsi