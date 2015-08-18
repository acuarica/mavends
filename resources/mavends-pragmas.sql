
/*
 * https://www.sqlite.org/pragma.html#pragma_page_size
 * 
 * Better performance by using the same page size as the OS?
 */
--pragma page_size = 4096;

/*
 * https://www.sqlite.org/pragma.html#pragma_journal_mode
 * 
 * Better performance by disabling journaling.
 * No need for journal since the db once is built, becomes read-only.
 */
--pragma journal_mode = off;
