
DB=out/mavenclass-last.sqlite3
# SQLITE_FLAGS=-echo

runmavenclassqueries:
	cat sql/mavenclass-queries.sql | sqlite3 $(SQLITE_FLAGS) $(DB)
