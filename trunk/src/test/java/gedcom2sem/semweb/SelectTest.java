package gedcom2sem.semweb;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SelectTest
{
    private static final String GEDCOM_TTL = new File("src/test/resources/kennedy.ttl").getAbsolutePath();
    private static final String MASHUP_TTL = new File("src/test/resources/mashup.ttl").getAbsolutePath();
    private static final String REPORT_DIR = new File("src/main/resources/reports").getAbsolutePath();
    private static final String REPORT_TXT = new File("target/report.txt").getAbsolutePath();
    private final Integer expectedNrOfLines;
    private final String queryFileName;
    private final Boolean mashup;

    public SelectTest(final Boolean mashup, final Integer expectedNrOfLines, final String queryFileName)
    {
        this.mashup = mashup;
        this.expectedNrOfLines = expectedNrOfLines;
        this.queryFileName = queryFileName;
    }

    @Parameters
    public static Collection<Object[]> getContructorParameters()
    {
        return Arrays.asList(new Object[][] {//
                {false, 16, "AgeDiffBetweenSpouses.arq"},//
                {false, 16, "CountEventsPerPlace.arq"},//
                {false, 16, "CountFirstNames.arq"},//
                {false, 16, "CountGivnNames.arq"},//
                {false, 16, "SOSA-InbredStatistics.arq"},//
                {false, 16, "SOSA-MultiMedia.arq"},//
                {true, 16, "mashup/classmates.arq"},//
                {true, 16, "mashup/dbpediaLanguages.arq"},//
                {true, 16, "mashup/dbpediaProperties.arq"},//
                {true, 16, "mashup/dbpediaRelatedEntities.arq"},//
                {true, 16, "mashup/geonamesProperties.arq"},//
                {true, 16, "mashup/geonamesRelatedEntities.arq"},//
                {true, 16, "mashup/mashup.arq"},//
                {true, 16, "mashup/MigrationLines.arq"},//
                });
    }

    @Test
    public void run() throws Exception
    {
        String qualifiedQueryFileName = REPORT_DIR +"/"+ queryFileName;
        if (mashup)
            Select.main(GEDCOM_TTL, MASHUP_TTL, REPORT_TXT, qualifiedQueryFileName);
        else
            Select.main(GEDCOM_TTL, REPORT_TXT, qualifiedQueryFileName);

        assertThat(qualifiedQueryFileName, countLines(REPORT_TXT), is(expectedNrOfLines));
    }

    private int countLines(String reportTxt) throws FileNotFoundException, IOException
    {
        final BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(reportTxt)));
        int nrOfLines = 0;
        while (bufferedReader.readLine() != null)
            nrOfLines++;
        bufferedReader.close();
        return nrOfLines;
    }
}
