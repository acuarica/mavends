package ch.usi.inf.mavends.cast;

import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;
import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.extract.Artifact;
import ch.usi.inf.mavends.util.extract.JarReader;
import ch.usi.inf.mavends.util.log.Log;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Main {

    private static final Log log = new Log(System.out);

    public static class Args {

        @Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the path of the Maven Index DB.")
        String mavenIndex;

        @Arg(key = "repo", name = "Maven Inode DB path", desc = "Specifies the path of the output db file.")
        String repo;

        @Arg(key = "query", name = "URI list", desc = "Specifies the output uri list file (*aria2* format).")
        String query;

    }

    public static void main(String[] args) throws Exception {
        final Args ar = ArgsParser.parse(args, new Args());
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        try (final Db db = new Db(ar.mavenIndex); final ResultSet rs = db.select(ar.query)) {

            Runtime.getRuntime().addShutdownHook(new Thread(
                    () -> log.info("Shutdown hook ...")));

            JarReader jr = new JarReader(ar.repo) {
                @Override
                public void processEntry(Artifact artifact, String fileName, byte[] fileData) {
                    try {
//                        mv.visitFileEntry(artifact, fileName, fileData);
                    } catch (Exception e) {
                        log.info("Exception: %s", e);
                    }
                }

                @Override
                public void processFileNotFound() {
//                	mv.filesNotFound++;
                }
            };

            int n = 0;

            while (rs.next()) {
                final long coordid = rs.getLong("coordid");
                final String groupid = rs.getString("groupid");
                final String artifactid = rs.getString("artifactid");
                final String version = rs.getString("version");
                final Date idate = df.parse(rs.getString("idate"));
                final Date mdate = df.parse(rs.getString("mdate"));
                final String path = rs.getString("path");

                final Artifact art = new Artifact(coordid, groupid, artifactid, version, idate, mdate, path);
                jr.process(art, art.path);

                n++;

                if (n % 1000 == 0) {
                    log.info("Remaining Jars: %,d", n);
                }
            }

            log.info("No. jar files: %,d", n);
        }
    }
}
