package com.latmod.mods.projectex.search;

import org.junit.Assert;
import org.junit.Test;

public class SearchEngineTest {

    @Test
    public void testEmcNumberParsing() {
        Assert.assertEquals(100.0, ProjectEXSearchEngine.parseEmcNumber("100"), 0.001);
        Assert.assertEquals(1000.0, ProjectEXSearchEngine.parseEmcNumber("1k"), 0.001);
        Assert.assertEquals(2500000.0, ProjectEXSearchEngine.parseEmcNumber("2.5M"), 0.001);
        Assert.assertEquals(10000000000.0, ProjectEXSearchEngine.parseEmcNumber("10B"), 0.001);
        Assert.assertEquals(10000000000.0, ProjectEXSearchEngine.parseEmcNumber("10G"), 0.001);
        Assert.assertEquals(1e12, ProjectEXSearchEngine.parseEmcNumber("1T"), 0.001);
        Assert.assertEquals(1e15, ProjectEXSearchEngine.parseEmcNumber("1P"), 0.001);
        Assert.assertEquals(1e18, ProjectEXSearchEngine.parseEmcNumber("1E"), 0.001);
        Assert.assertEquals(50000.0, ProjectEXSearchEngine.parseEmcNumber("50,000"), 0.001);
    }

    @Test
    public void testQueryParsing() {
        // Empty query
        ProjectEXSearchEngine.IQueryPredicate empty = ProjectEXSearchEngine.parseQuery("");
        Assert.assertTrue(empty instanceof ProjectEXSearchEngine.AlwaysTruePredicate);

        // Mod query
        ProjectEXSearchEngine.IQueryPredicate mod = ProjectEXSearchEngine.parseQuery("@projectex");
        Assert.assertTrue(mod instanceof ProjectEXSearchEngine.ModFilter);

        // Range query
        ProjectEXSearchEngine.IQueryPredicate range = ProjectEXSearchEngine.parseQuery("#1000-50000");
        Assert.assertTrue(range instanceof ProjectEXSearchEngine.EmcRangeFilter);

        // Greater than / Less than
        ProjectEXSearchEngine.IQueryPredicate gt = ProjectEXSearchEngine.parseQuery("#>10k");
        Assert.assertTrue(gt instanceof ProjectEXSearchEngine.EmcRangeFilter);

        ProjectEXSearchEngine.IQueryPredicate lt = ProjectEXSearchEngine.parseQuery("#<5M");
        Assert.assertTrue(lt instanceof ProjectEXSearchEngine.EmcRangeFilter);

        ProjectEXSearchEngine.IQueryPredicate plus = ProjectEXSearchEngine.parseQuery("#500k+");
        Assert.assertTrue(plus instanceof ProjectEXSearchEngine.EmcRangeFilter);

        // Negation
        ProjectEXSearchEngine.IQueryPredicate neg = ProjectEXSearchEngine.parseQuery("!@minecraft");
        Assert.assertTrue(neg instanceof ProjectEXSearchEngine.NegatedPredicate);

        // Tag / OreDict
        ProjectEXSearchEngine.IQueryPredicate ore = ProjectEXSearchEngine.parseQuery("$ingot");
        Assert.assertTrue(ore instanceof ProjectEXSearchEngine.OreDictFilter);

        // Tooltip
        ProjectEXSearchEngine.IQueryPredicate tip = ProjectEXSearchEngine.parseQuery("%energy");
        Assert.assertTrue(tip instanceof ProjectEXSearchEngine.TooltipFilter);

        // Registry ID
        ProjectEXSearchEngine.IQueryPredicate reg = ProjectEXSearchEngine.parseQuery("id:matter");
        Assert.assertTrue(reg instanceof ProjectEXSearchEngine.RegistryIdFilter);

        // Category
        ProjectEXSearchEngine.IQueryPredicate fuel = ProjectEXSearchEngine.parseQuery("^fuel");
        Assert.assertTrue(fuel instanceof ProjectEXSearchEngine.FuelFilter);

        // Affordable
        ProjectEXSearchEngine.IQueryPredicate aff = ProjectEXSearchEngine.parseQuery("#aff");
        Assert.assertTrue(aff instanceof ProjectEXSearchEngine.AffordableFilter);

        // OR Query
        ProjectEXSearchEngine.IQueryPredicate orPred = ProjectEXSearchEngine.parseQuery("copper | tin");
        Assert.assertTrue(orPred instanceof ProjectEXSearchEngine.OrPredicate);

        // Compound AND Query
        ProjectEXSearchEngine.IQueryPredicate andPred = ProjectEXSearchEngine.parseQuery("@projectex star #1M-100M !colossal");
        Assert.assertTrue(andPred instanceof ProjectEXSearchEngine.AndPredicate);

        // Quoted String Query
        ProjectEXSearchEngine.IQueryPredicate quotePred = ProjectEXSearchEngine.parseQuery("\"dark matter\"");
        Assert.assertTrue(quotePred instanceof ProjectEXSearchEngine.DisplayNameFilter);
    }

    @Test
    public void testSearchHistoryNavigation() {
        SearchHistoryManager.resetCursor();
        SearchHistoryManager.addHistory("@projectex");
        SearchHistoryManager.addHistory("#1k-50k");
        SearchHistoryManager.addHistory("ingot");

        // Navigate Up
        String q1 = SearchHistoryManager.navigateUp("");
        Assert.assertEquals("ingot", q1);

        String q2 = SearchHistoryManager.navigateUp(q1);
        Assert.assertEquals("#1k-50k", q2);

        String q3 = SearchHistoryManager.navigateUp(q2);
        Assert.assertEquals("@projectex", q3);

        // Top limit
        String qTop = SearchHistoryManager.navigateUp(q3);
        Assert.assertEquals("@projectex", qTop);

        // Navigate Down
        String d1 = SearchHistoryManager.navigateDown(qTop);
        Assert.assertEquals("#1k-50k", d1);

        String d2 = SearchHistoryManager.navigateDown(d1);
        Assert.assertEquals("ingot", d2);

        String dBottom = SearchHistoryManager.navigateDown(d2);
        Assert.assertEquals("", dBottom);
    }
}
