/*
 * Created by Kultala Aki on 8/1/21, 10:25 PM
 * Copyright (c) 2021. All rights reserved.
 * Last modified 8/1/21, 10:25 PM
 */

package kultalaaki.vpkapuri.alarms;


import java.util.ArrayList;
import java.util.List;

/**
 * Object contains list with all cities in Finland
 */
public class Cities {

    List<String> cities;

    public Cities() {
        this.cities = new ArrayList<>();
        addCitiesToList();
    }

    /**
     * @return ArrayList<String> containing all cities of Finland.
     */
    public List<String> getCityList() {
        return this.cities;
    }

    private void addCitiesToList() {
        cities.add("Akaa");
        cities.add("Alajärvi");
        cities.add("Alavieska");
        cities.add("Alavus");
        cities.add("Asikkala");
        cities.add("Askola");
        cities.add("Aura");
        cities.add("Brändö");
        cities.add("Eckerö");
        cities.add("Enonkoski");
        cities.add("Enontekiö");
        cities.add("Espoo");
        cities.add("Eura");
        cities.add("Eurajoki");
        cities.add("Evijärvi");
        cities.add("Finström");
        cities.add("Forssa");
        cities.add("Föglö");
        cities.add("Geta");
        cities.add("Haapajärvi");
        cities.add("Haapavesi");
        cities.add("Hailuoto");
        cities.add("Halsua");
        cities.add("Hamina");
        cities.add("Hammarland");
        cities.add("Hankasalmi");
        cities.add("Hanko");
        cities.add("Harjavalta");
        cities.add("Hartola");
        cities.add("Hattula");
        cities.add("Hausjärvi");
        cities.add("Heinola");
        cities.add("Heinävesi");
        cities.add("Helsinki");
        cities.add("Hirvensalmi");
        cities.add("Hollola");
        cities.add("Honkajoki");
        cities.add("Huittinen");
        cities.add("Humppila");
        cities.add("Hyrynsalmi");
        cities.add("Hyvinkää");
        cities.add("Hämeenkyrö");
        cities.add("Hämeenlinna");
        cities.add("Ii");
        cities.add("Iisalmi");
        cities.add("Iitti");
        cities.add("Ikaalinen");
        cities.add("Ilmajoki");
        cities.add("Ilomantsi");
        cities.add("Imatra");
        cities.add("Inari");
        cities.add("Pohjois-lapinseutukunta");
        cities.add("Pedersöre");
        cities.add("Inkoo");
        cities.add("Isojoki");
        cities.add("Isokyrö");
        cities.add("Janakkala");
        cities.add("Joensuu");
        cities.add("Jokioinen");
        cities.add("Jomala");
        cities.add("Joroinen");
        cities.add("Joutsa");
        cities.add("Juuka");
        cities.add("Juupajoki");
        cities.add("Juva");
        cities.add("Jyväskylä");
        cities.add("Jämijärvi");
        cities.add("Jämsä");
        cities.add("Järvenpää");
        cities.add("Kaarina");
        cities.add("Kaavi");
        cities.add("Kajaani");
        cities.add("Kalajoki");
        cities.add("Kangasala");
        cities.add("Kangasniemi");
        cities.add("Kankaanpää");
        cities.add("Kannonkoski");
        cities.add("Kannus");
        cities.add("Karijoki");
        cities.add("Karkkila");
        cities.add("Karstula");
        cities.add("Karvia");
        cities.add("Kaskinen");
        cities.add("Kauhajoki");
        cities.add("Kauhava");
        cities.add("Kauniainen");
        cities.add("Kaustinen");
        cities.add("Keitele");
        cities.add("Kemi");
        cities.add("Kemijärvi");
        cities.add("Keminmaa");
        cities.add("Kemiönsaari");
        cities.add("Kempele");
        cities.add("Kerava");
        cities.add("Keuruu");
        cities.add("Kihniö");
        cities.add("Kinnula");
        cities.add("Kirkkonummi");
        cities.add("Kitee");
        cities.add("Kittilä");
        cities.add("Kiuruvesi");
        cities.add("Kivijärvi");
        cities.add("Kokemäki");
        cities.add("Kokkola");
        cities.add("Kolari");
        cities.add("Konnevesi");
        cities.add("Kontiolahti");
        cities.add("Korsnäs");
        cities.add("Koskitl");
        cities.add("Kotka");
        cities.add("Kouvola");
        cities.add("Kristiinankaupunki");
        cities.add("Kruunupyy");
        cities.add("Kuhmo");
        cities.add("Kuhmoinen");
        cities.add("Kumlinge");
        cities.add("Kuopio");
        cities.add("Kuortane");
        cities.add("Kurikka");
        cities.add("Kustavi");
        cities.add("Kuusamo");
        cities.add("Kyyjärvi");
        cities.add("Kyröskoski");
        cities.add("Kärkölä");
        cities.add("Kärsämäki");
        cities.add("Kökar");
        cities.add("Lahti");
        cities.add("Laihia");
        cities.add("Laitila");
        cities.add("Lapinjärvi");
        cities.add("Lapinlahti");
        cities.add("Lappajärvi");
        cities.add("Lappeenranta");
        cities.add("Lapua");
        cities.add("Laukaa");
        cities.add("Lemi");
        cities.add("Lemland");
        cities.add("Lempäälä");
        cities.add("Leppävirta");
        cities.add("Lestijärvi");
        cities.add("Lieksa");
        cities.add("Lieto");
        cities.add("Liminka");
        cities.add("Liperi");
        cities.add("Lohja");
        cities.add("Loimaa");
        cities.add("Loppi");
        cities.add("Loviisa");
        cities.add("Luhanka");
        cities.add("Lumijoki");
        cities.add("Lumparland");
        cities.add("Luoto");
        cities.add("Luumäki");
        cities.add("Maalahti");
        cities.add("Maarianhamina");
        cities.add("Marttila");
        cities.add("Masku");
        cities.add("Merijärvi");
        cities.add("Merikarvia");
        cities.add("Miehikkälä");
        cities.add("Mikkeli");
        cities.add("Muhos");
        cities.add("Multia");
        cities.add("Muonio");
        cities.add("Mustasaari");
        cities.add("Muurame");
        cities.add("Mynämäki");
        cities.add("Myrskylä");
        cities.add("Mäntsälä");
        cities.add("Mänttä-vilppula");
        cities.add("Mänttä");
        cities.add("Vilppula");
        cities.add("Mäntyharju");
        cities.add("Naantali");
        cities.add("Nakkila");
        cities.add("Nivala");
        cities.add("Nokia");
        cities.add("Nousiainen");
        cities.add("Nurmes");
        cities.add("Nurmijärvi");
        cities.add("Närpiö");
        cities.add("Orimattila");
        cities.add("Oripää");
        cities.add("Orivesi");
        cities.add("Oulainen");
        cities.add("Oulu");
        cities.add("Outokumpu");
        cities.add("Padasjoki");
        cities.add("Paimio");
        cities.add("Paltamo");
        cities.add("Parainen");
        cities.add("Parikkala");
        cities.add("Parkano");
        cities.add("Pedersörenkunta");
        cities.add("Pedersöre");
        cities.add("Pelkosenniemi");
        cities.add("Pello");
        cities.add("Perho");
        cities.add("Pertunmaa");
        cities.add("Petäjävesi");
        cities.add("Pieksämäki");
        cities.add("Pielavesi");
        cities.add("Pietarsaari");
        cities.add("Pihtipudas");
        cities.add("Pirkkala");
        cities.add("Polvijärvi");
        cities.add("Pomarkku");
        cities.add("Pori");
        cities.add("Pornainen");
        cities.add("Porvoo");
        cities.add("Posio");
        cities.add("Pudasjärvi");
        cities.add("Pukkila");
        cities.add("Punkalaidun");
        cities.add("Puolanka");
        cities.add("Puumala");
        cities.add("Pyhtää");
        cities.add("Pyhäjoki");
        cities.add("Pyhäjärvi");
        cities.add("Pyhäntä");
        cities.add("Pyhäranta");
        cities.add("Pälkäne");
        cities.add("Pöytyä");
        cities.add("Raahe");
        cities.add("Raasepori");
        cities.add("Raisio");
        cities.add("Rantasalmi");
        cities.add("Ranua");
        cities.add("Rauma");
        cities.add("Rautalampi");
        cities.add("Rautavaara");
        cities.add("Rautjärvi");
        cities.add("Reisjärvi");
        cities.add("Riihimäki");
        cities.add("Ristijärvi");
        cities.add("Rovaniemi");
        cities.add("Ruokolahti");
        cities.add("Ruovesi");
        cities.add("Rusko");
        cities.add("Rääkkylä");
        cities.add("Saarijärvi");
        cities.add("Salla");
        cities.add("Salo");
        cities.add("Saltvik");
        cities.add("Sastamala");
        cities.add("Sauvo");
        cities.add("Savitaipale");
        cities.add("Savonlinna");
        cities.add("Savukoski");
        cities.add("Seinäjoki");
        cities.add("Sievi");
        cities.add("Siikainen");
        cities.add("Siikajoki");
        cities.add("Siikalatva");
        cities.add("Siilinjärvi");
        cities.add("Simo");
        cities.add("Sipoo");
        cities.add("Siuntio");
        cities.add("Sodankylä");
        cities.add("Soini");
        cities.add("Somero");
        cities.add("Sonkajärvi");
        cities.add("Sotkamo");
        cities.add("Sottunga");
        cities.add("Sulkava");
        cities.add("Sund");
        cities.add("Suomussalmi");
        cities.add("Suonenjoki");
        cities.add("Sysmä");
        cities.add("Säkylä");
        cities.add("Taipalsaari");
        cities.add("Taivalkoski");
        cities.add("Taivassalo");
        cities.add("Tammela");
        cities.add("Tampere");
        cities.add("Tervo");
        cities.add("Tervola");
        cities.add("Teuva");
        cities.add("Tohmajärvi");
        cities.add("Toholampi");
        cities.add("Toivakka");
        cities.add("Tornio");
        cities.add("Turku");
        cities.add("Tuusniemi");
        cities.add("Tuusula");
        cities.add("Tyrnävä");
        cities.add("Ulvila");
        cities.add("Urjala");
        cities.add("Utajärvi");
        cities.add("Utsjoki");
        cities.add("Uurainen");
        cities.add("Uusikaarlepyy");
        cities.add("Uusikaupunki");
        cities.add("Vaala");
        cities.add("Vaasa");
        cities.add("Valkeakoski");
        cities.add("Valtimo");
        cities.add("Vantaa");
        cities.add("Varkaus");
        cities.add("Vehmaa");
        cities.add("Vesanto");
        cities.add("Vesilahti");
        cities.add("Veteli");
        cities.add("Vieremä");
        cities.add("Vihti");
        cities.add("Viitasaari");
        cities.add("Vimpeli");
        cities.add("Virolahti");
        cities.add("Virrat");
        cities.add("Vårdö");
        cities.add("Vöyri");
        cities.add("Ylitornio");
        cities.add("Ylivieska");
        cities.add("Ylöjärvi");
        cities.add("Ypäjä");
        cities.add("Ähtäri");
        cities.add("Äänekoski");
    }
}
