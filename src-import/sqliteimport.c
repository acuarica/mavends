

#include <stdio.h>
#include <string.h>
#include "sqlite3.h"

int main(int argc, const char* argv[]) {

	for (int i = 0; i < argc; i++) {
		fprintf(stderr, "%s\n", argv[i]);
	}

	sqlite3* db;
	char* errmsg = NULL;

	int err = sqlite3_open("../out/es.sqlite3", &db);
	if (err) {
		fprintf(stderr, "error: %s\n", sqlite3_errmsg(db));
		sqlite3_close(db);

		return 1;
	}

	sqlite3_exec(db, "pragma journal_mode=off", NULL, NULL, &errmsg);
	sqlite3_exec(db, "pragma count_changes=off", NULL, NULL, &errmsg);
	//sqlite3_exec(db, "begin transaction", NULL, NULL, &errmsg);


	sqlite3_exec(db, "begin transaction", NULL, NULL, &errmsg);

	const char* sql = "insert into inode (originalsize, compressedsize, crc32, sha1) values (?1, ?2, ?3, ?4)";

	sqlite3_stmt* stmt;
	sqlite3_prepare_v2(db, sql, strlen(sql), &stmt, NULL);
	
	for (int i = 0; i < 5000000; i++) {
		sqlite3_bind_int(stmt, 1, i);
		sqlite3_bind_int(stmt, 2, i);
		sqlite3_bind_int(stmt, 3, i);
		sqlite3_bind_int(stmt, 4, i);
		//sqlite3_bind_int(stmt, 5, NULL);

		err = sqlite3_step(stmt);
		if (err != SQLITE_DONE) {
			fprintf(stderr, "SQL error: %d\n", err);
			//sqlite3_free(errmsg);
			sqlite3_close(db);

			return 2;
		}

		sqlite3_reset(stmt);
	}

	sqlite3_exec(db, "commit transaction", NULL, NULL, &errmsg);

	sqlite3_close(db);

}

