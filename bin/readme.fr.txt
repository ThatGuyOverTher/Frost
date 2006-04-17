* Frost - A propos de Freenet *
_-----------------------------_


Frost peut fonctionner avec Freenet 0.5 et Freenet 0.7. Vous choisissez lors
du premier d�marrage la version de Freenet que vous utilisez. Chaque
installation de Frost ne peut fonctionner qu'avec une seule version de Freenet.
Vous devrez faire fonctionner deux version de Frost diff�rentes si vous
souhaitez acc�der aux deux r�seaux, parce que le format des cl�s est diff�rent.

Frost suppose qu'il est d�marr� pour la premi�re fois s'il ne trouve pas le
fichier identities.xml. La fen�tre affich�e lors du permier d�marrage vous
permet de choisir la version de Freenet et, optionnellement, d'importer un
fichier identities.xml existant � partir d'une installation de Frost existante
(0.5 ou 0.7).


Mise � jour de Frost 0.5 ou 0.7 vers une nouvelle version:
----------------------------------------------------------
Si Frost est d�marr�, arr�tez-le, puis copiez le contenu du fichier ZIP
t�l�charg� dans le r�pertoire o� est install� Frost, en �crasant tous les
fichiers existants. Tous vos param�tres seront conserv�s. Comme toujours,
il est recommand� de faire une sauvegarde avant la mise � jour. :)


Vous utilisez d�ja Frost 0.5 et vous voulez d�marrer Frost 0.7 (ou vice versa):
-------------------------------------------------------------------------------
Copiez le contenu  du fichier ZIP t�l�charg� dans un NOUVEAU r�pertoire et
lancez Frost. Dans la premi�re fen�tre de d�marrage, choisissez la version
de Freenet que vous souhaitez utiliser et �ventuellement le fichier
identities.xml que vous souhaitez importer d'une insatllation de Frost
pr�c�dente (il est recommand� d'arr�ter l'ancien Frost avant l'importation).
Ou vous pouvez d�cider de cr�er une nouvelle identit�.


En cas de probl�me:
-------------------
Frost suppose que votre noeud Freenet tourne sur la m�me machine, avec le num�ro
de port FCP par d�faut. Pour Freenet 0.5 c'est "127.0.0.1:8481", et pour
Freenet 0.7 c'est "127.0.0.1:9481". Si votre noeud Freenet tourne sur une autre
machine, ou si vous avez configur� FCP pour un autre num�ro de port, le premier
d�marrage �chouera. Dans ce cas, vous devrez �diter le fichier "frost.ini" qui
se trouve dans le r�pertoire "config". Ce fichier est g�n�r� automatiquement
durant le premier d�marrage de Frost. Ouvrez le fichier "frost.ini" et trouvez
la ligne contenant "availableNodes=127.0.0.1:8481". Changez le param�tre pour
qu'il corresponde � vos besoins ("availableNodes=autrenoeud:12345", par exemple),
et d�marrez Frost.
Il devrait maintenant �tre capable de se connecter � votre noeud.
Faites attention � bien configurer votre noeud Freenet pour qu'il accepte les
connexions FCP en provenance d'autres machines que lui-m�me ! Apr�s le d�marrage
de Frost, vous pourrez � nouveau changer ce param�tre depuis la fen�tre de
configuration.

Si (et seulement si) vous avez choisi la mauvaise version de Freenet durant la
mise � jour, vous pouvez changer cela dans le fichier "frost.ini". Trouvez la
ligne "freenetVersion=" et mettez-y "freenetVersion=05" ou "freenetVersion=07".


Note � l'attention des UNIXiens:
--------------------------------
Apr�s l'extraction du fichier ZIP, les fichiers "*.sh" ne seront pas ex�cutables
sur votre syst�me. Ex�cutez un "chmod +x *.sh" dans le r�pertoire de Frost pour
rem�dier � ce probl�me.
