package gedcom2sem.semweb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public abstract class QueryTest
{
     static final String GEDCOM = new File("src/test/resources/kennedy.ged").getAbsolutePath();
     static final String GEDCOM_TTL = new File("src/test/resources/kennedy.ttl").getAbsolutePath();
     static final String MASHUP_TTL = new File("src/test/resources/mashup.ttl").getAbsolutePath();
     static final String REPORT_DIR = new File("src/main/resources/reports").getAbsolutePath();
     static final String REPORT_TXT = new File("target/report.txt").getAbsolutePath();
     final Integer expectedNrOfLines;
     final String queryFileName;
     final Boolean mashup;

    public QueryTest(final Boolean mashup, final Integer expectedNrOfLines, final String queryFileName)
    {
        this.mashup = mashup;
        this.expectedNrOfLines = expectedNrOfLines;
        this.queryFileName = REPORT_DIR +"/"+queryFileName;
    }

    @Parameters
    public static Collection<Object[]> getContructorParameters()
    {
        return Arrays.asList(new Object[][] {//
                {false, 1, "AgeDiffBetweenSpouses.arq"},//
                {false, 89, "CountEventsPerPlace.arq"},//
                {false, 1, "CountFirstNames.arq"},//
                {false, 7, "CountGivnNames.arq"},//
                {false, 1, "SOSA-InbredStatistics.arq"},//
                {false, 1, "SOSA-MultiMedia.arq"},//
                {true, 1, "mashup/classmates.arq"},//
                {true, 1, "mashup/dbpediaLanguages.arq"},//
                {true, 1, "mashup/dbpediaProperties.arq"},//
                {true, 1, "mashup/dbpediaRelatedEntities.arq"},//
                {true, 1, "mashup/geonamesProperties.arq"},//
                {true, 1, "mashup/geonamesRelatedEntities.arq"},//
                {true, 1, "mashup/mashup.arq"},//
                {true, 1, "mashup/MigrationLines.arq"},//
                });
    }

    int countLines(String reportTxt) throws FileNotFoundException, IOException
    {
        final BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(reportTxt)));
        int nrOfLines = 0;
        while (bufferedReader.readLine() != null)
            nrOfLines++;
        bufferedReader.close();
        return nrOfLines;
    }
}
