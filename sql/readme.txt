
1. F�r att skapa en ny databas, g�r s� h�r.

1. K�r scriptet tables.ascii.sql. Om du k�r mot en nyskapad db utan tabeller i, kommer
scriptet att skrika att det inte kan hitta tabellerna. Helt OK.

2. K�r scriptet sql/data/types.sql.

3. K�r scriptet sprocs.ascii.sql. Scriptet gn�ller att den inte kan addera rader i
sysdepends. Ok.

4. Om det �r en nyskapad db. K�r �ven scriptet sql/data/newdb.sql. Det adderar
in den f�rsta rollen, resp anv�ndaren., mallgruppen, mallen etc. Det som
beh�vs f�r att komma ig�ng med databasen.