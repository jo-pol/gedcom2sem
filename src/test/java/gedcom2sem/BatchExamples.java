package gedcom2sem;

import gedcom2sem.gedsem.Convert;
import gedcom2sem.gedsem.UriFormats;
import gedcom2sem.semweb.Mashup;
import gedcom2sem.semweb.Select;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.junit.Ignore;
import org.junit.Test;

public class BatchExamples
{
    private static final String QUICK_RULES = new File("src/main/resources/rules/QuickRules.txt").getAbsolutePath();
    private static final String MASHUP_ARQ = new File("src/main/resources/reports/mashup/mashup.arq").getAbsolutePath();
    private static final String MIGRATIONS_ARQ = new File("src/main/resources/reports/mashup/MigrationLines.arq").getAbsolutePath();
    private static final String GEDCOM = new File("src/test/resources/kennedy.ged").getAbsolutePath();
    private static final String GEDCOM_TTL = new File("target/kennedy.ttl").getAbsolutePath();
    private static final String MASHUP_TSV = new File("target/mashup.tsv").getAbsolutePath();
    private static final String MASHUP_TTL = new File("target/mashup.ttl").getAbsolutePath();
    private static final String REPORT_TXT = new File("target/report.txt").getAbsolutePath();

    @Ignore // takes too long
    @Test
    public void kennedy() throws Exception
    {
        //TODO create MASHUP_TTL/TSV ???
        final PrintStream saved = redirectOutput(GEDCOM_TTL);
        Convert.main("-rules", QUICK_RULES, "-uri", UriFormats.DEFAULT_URI, "-gedcom", GEDCOM);
        restoreOutput(saved);
        Select.main(GEDCOM_TTL, MASHUP_TSV, MASHUP_ARQ);
        Mashup.main(GEDCOM_TTL, MASHUP_TSV, MASHUP_TTL, UriFormats.DEFAULT_URI, "de|fr");
        Select.main(GEDCOM_TTL, MASHUP_TTL, REPORT_TXT, MIGRATIONS_ARQ);
    }

    private PrintStream redirectOutput(final String gedcomTtl) throws FileNotFoundException
    {
        final PrintStream saved = System.out;
        System.setOut(new PrintStream(new FileOutputStream(gedcomTtl)));
        return saved;
    }
    
    private void restoreOutput(final PrintStream saved)
    {
        System.setOut(saved);
    }
}
