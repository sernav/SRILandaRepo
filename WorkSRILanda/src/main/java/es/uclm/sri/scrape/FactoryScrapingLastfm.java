package main.java.es.uclm.sri.scrape;

import main.java.es.uclm.sri.scrape.webscraping.ScrapingLastfm;

/**
 * Factoría concreta de scraping en plataforma Lastfm
 * 
 * @author Sergio Navarro
 * */
public class FactoryScrapingLastfm extends AbstractFactoryScraping {

    public AbstractWebScraping launcherScraping(String rutaDestino) {
        return new ScrapingLastfm(rutaDestino);
    }

}