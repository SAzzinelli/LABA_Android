package com.laba.firenze.domain.model

/**
 * Descrizioni dettagliate per ogni achievement, come su iOS AchievementViews.swift.
 * Usate nella sezione "Descrizione" del dialog dei dettagli.
 */
object AchievementDetailedDescriptions {
    
    fun get(achievement: Achievement): String = when (achievement.id) {
        // Primi Passi
        "first_login" -> "Benvenuto nella famiglia LABA! Hai fatto il primo passo nel tuo percorso accademico digitale. Questo è solo l'inizio di un viaggio ricco di traguardi e soddisfazioni."
        "first_data" -> "Hai caricato i tuoi dati per la prima volta! Ora l'app conosce il tuo percorso e può aiutarti a raggiungere i tuoi obiettivi accademici. Ogni dato caricato ti avvicina ai tuoi traguardi."
        
        // Esami
        "first_18" -> "Il primo diciotto è sempre speciale! Celebra il tuo primo esame superato con il voto minimo. È il punto di partenza del tuo percorso accademico e dimostra che hai iniziato il tuo cammino verso la laurea."
        "first_30" -> "Eccellenza assoluta! Il tuo primo trenta rappresenta la massima soddisfazione accademica. Questo voto dimostra non solo la tua preparazione, ma anche la tua dedizione e passione per la materia."
        "first_lode" -> "Un traguardo straordinario! La prima lode è il riconoscimento più alto che puoi ottenere. Rappresenta non solo la perfetta conoscenza della materia, ma anche la tua capacità di andare oltre le aspettative."
        "first_exam_booked" -> "Il dado è tratto! Hai prenotato il tuo primo esame, dimostrando coraggio e determinazione. Ogni esame prenotato è un passo avanti verso il tuo obiettivo finale."
        "ready_to_graduate" -> "Sei pronto per il grande passo! Indica che hai completato tutti gli esami necessari per laurearti. È il momento di preparare la tesi e guardare verso il futuro professionale."
        "year_1_complete" -> "Primo anno completato con successo! Hai superato tutti gli esami del primo anno, dimostrando costanza e impegno. Il percorso è ancora lungo, ma hai già fatto un grande passo."
        "year_2_complete" -> "Secondo anno dominato! Hai dimostrato di saper gestire la complessità crescente degli studi. Sei a metà del percorso e la meta si avvicina sempre di più."
        "year_3_complete" -> "Terzo anno conquistato! Hai completato tutti gli esami del triennio. Ora sei pronto per la tesi e per il grande traguardo finale: la laurea!"
        
        // Performance
        "streak_perfect" -> "Streak perfetto! Hai superato 3 esami consecutivi con voto ≥ 28, dimostrando una preparazione costante e di alto livello. La tua dedizione brilla in ogni esame."
        "perfezionista" -> "Cinque lodi sono un risultato eccezionale! Dimostra la tua costante ricerca dell'eccellenza accademica. Sei un vero perfezionista che non si accontenta mai del minimo."
        "punteggio_pieno" -> "Punteggio pieno raggiunto! Una media finale ≥ 28 è il risultato di anni di studio costante e dedizione. Sei tra i migliori studenti dell'accademia."
        "maratoneta" -> "Maratoneta accademico! Hai superato 3 esami in una singola sessione, dimostrando resistenza, organizzazione e determinazione. Un vero atleta dello studio!"
        
        // Seminari
        "first_seminar" -> "Curioso e intraprendente! Hai frequentato il tuo primo seminario, dimostrando interesse per l'approfondimento e la crescita personale oltre il programma di studi."
        "two_seminars" -> "Appassionato di conoscenza! Con 2 seminari frequentati dimostri un interesse genuino per l'apprendimento continuo e l'arricchimento del tuo bagaglio culturale."
        "three_seminars" -> "Assetato di conoscenza! 3 seminari frequentati mostrano la tua voglia di andare oltre il minimo necessario. La curiosità è il motore del tuo successo."
        "five_seminars" -> "Collezionista di seminari! Con 5 seminari hai dimostrato un impegno straordinario nell'approfondimento. Ogni seminario è un'opportunità di crescita che hai saputo cogliere."
        "first_seminar_booked" -> "Primo passo! Hai prenotato il tuo primo seminario. La curiosità e la proattività sono il primo step verso l'arricchimento del tuo percorso di studi."
        "two_seminars_booked" -> "In agenda! Hai prenotato i tuoi primi 2 seminari. Stai pianificando il tuo arricchimento formativo con determinazione."
        "three_seminars_booked" -> "Organizzato! Hai prenotato i tuoi primi 3 seminari. La tua agenda formativa sta prendendo forma."
        "five_seminars_booked" -> "Pianificatore! Hai prenotato i tuoi primi 5 seminari. La tua proattività nell'approfondimento è ammirevole."
        "networking" -> "Networking attivo! Hai partecipato a 10 eventi e seminari, costruendo relazioni e ampliando la tua rete di contatti. Il networking è fondamentale per il tuo futuro professionale."
        
        // CFA
        "cfa_25" -> "25% del percorso completato! Hai raggiunto il primo quarto dei crediti necessari. Il cammino è ancora lungo, ma hai già dimostrato di saper procedere con costanza."
        "cfa_half" -> "A metà strada! Hai raggiunto il 50% dei crediti formativi necessari. Sei esattamente a metà del tuo percorso accademico. Continua così!"
        "cfa_75" -> "75% del percorso completato! Sei quasi alla fine. Hai accumulato la maggior parte dei crediti necessari e la meta finale è sempre più vicina."
        "cfa_complete" -> "Percorso completato al 100%! Hai raggiunto tutti i crediti formativi necessari per la tua laurea. Questo è un traguardo fondamentale che certifica la completezza del tuo percorso di studi."
        "cfa_collector" -> "CFA collector! Hai accumulato più crediti del necessario, dimostrando un impegno che va oltre i requisiti minimi. La tua dedizione non conosce limiti."
        
        // App Usage
        "puntuale" -> "Costanza e dedizione! Accedere all'app per 7 giorni consecutivi dimostra il tuo impegno nel rimanere aggiornato sui tuoi studi. È la base di una buona routine di studio."
        "sempre_aggiornato" -> "Sempre aggiornato! Leggere tutte le notifiche entro 24 ore per un mese dimostra la tua attenzione ai dettagli e la tua capacità di rimanere informato in tempo reale."
        "gufo_notturno" -> "Gufo notturno! Accedere all'app dopo mezzanotte 5 volte mostra che lo studio non ha orari per te. La notte è il tuo momento di concentrazione massima."
        "mattiniero" -> "Mattiniero! Accedere all'app prima delle 7:00 per 10 volte dimostra che sei una persona organizzata che inizia la giornata con impegno e determinazione."
        "dipendente" -> "Dipendente dall'app! 50 accessi dimostrano che l'app è diventata parte integrante della tua routine quotidiana. La tecnologia al servizio del tuo successo."
        "refresh_maniac" -> "Refresh maniac! 100 aggiornamenti mostrano la tua attenzione ai dettagli e la voglia di avere sempre i dati più aggiornati. La precisione è la tua forza."
        "esploratore" -> "Esploratore dell'app! Hai visitato tutte le sezioni, dimostrando curiosità e voglia di conoscere tutte le funzionalità a tua disposizione. Un vero navigatore digitale!"
        "studioso" -> "Studioso appassionato! Aprire 10 dispense diverse mostra la tua voglia di approfondire e studiare materiali aggiuntivi. La conoscenza è il tuo tesoro."
        "informato" -> "Informato e preparato! Leggere tutti i regolamenti dimostra la tua attenzione alle regole e la voglia di conoscere tutti i dettagli del tuo percorso accademico."
        "curioso" -> "Curioso e attento! Visitare le FAQ 5 volte mostra che non ti accontenti delle risposte superficiali e cerchi sempre di capire meglio. La curiosità è la chiave del sapere."
        
        // Easter Eggs
        "fortunato" -> "Fortunato! Ottenere esattamente 27 (il numero perfetto) è un segno del destino. Questo voto speciale porta con sé un significato unico nel tuo percorso."
        "arcobaleno" -> "Arcobaleno accademico! Avere nel libretto tutti i voti da 18 a 30L dimostra la varietà della tua esperienza. Ogni voto racconta una storia diversa del tuo percorso."
        "compleanno" -> "Compleanno studente! Accedere all'app il giorno del tuo compleanno è un modo speciale di celebrare. Anche nei giorni di festa, lo studio è parte di te."
        "natale" -> "Studente natalizio! Accedere durante le vacanze di Natale mostra che anche nei momenti di festa non dimentichi i tuoi obiettivi. La dedizione non va in vacanza."
        "estate_agosto" -> "Studente estivo! Studiare durante agosto dimostra una determinazione fuori dal comune. Mentre altri riposano, tu continui a lavorare per i tuoi obiettivi."
        "estate_giugno" -> "Sessione estiva completata! Superare 3 esami nella sessione di giugno richiede organizzazione e dedizione. Hai sfruttato al meglio il periodo estivo."
        "unicorno" -> "Primo dell'anno speciale! Accedere a mezzanotte del 1 gennaio è un modo unico di iniziare l'anno nuovo. Un inizio magico per un anno di successi."
        "halloween" -> "Halloween studente! Accedere il 31 ottobre aggiunge un tocco festoso al tuo studio. Anche nei momenti di svago, l'app è con te."
        
        // Meta
        "collezionista" -> "Stai costruendo una collezione impressionante! 10 achievement sbloccati dimostrano la tua dedizione e il tuo impegno nel raggiungere diversi obiettivi accademici."
        "maestro" -> "Un vero esperto! Con 20 achievement hai dimostrato una conoscenza approfondita del sistema e un impegno costante nel raggiungere i tuoi obiettivi accademici."
        "leggenda" -> "Leggendario! Hai sbloccato tutti gli achievement disponibili. Sei diventato una leggenda dell'accademia, dimostrando eccellenza in ogni aspetto del tuo percorso di studi."
        "cacciatore" -> "Cacciatore di achievement! Sbloccare 5 achievement in un solo giorno dimostra una determinazione straordinaria e la capacità di raggiungere obiettivi multipli con efficienza."
        "milionario" -> "Un vero maestro degli achievement! Raggiungere 1000 CFApp totali significa aver completato molti traguardi e dimostrato grande impegno nel tuo percorso accademico."
        
        // Traguardi
        "graduated" -> "LAUREATO! 🎊🎉 Ce l'hai fatta! Questo è il traguardo più importante del tuo percorso accademico. La laurea è il risultato di anni di studio, dedizione e sacrifici. Congratulazioni per questo incredibile risultato!"
        
        // LABArola (Giochi)
        "labarola_first_win" -> "Hai indovinato la parola del giorno! La prima vittoria in LABArola è un momento speciale. Continua a giocare per migliorare le tue competenze lessicali."
        "labarola_perfect" -> "Colpo perfetto! Indovinare la parola al primo tentativo richiede intuito e conoscenza. Un risultato raro e straordinario!"
        "labarola_5_wins" -> "5 vittorie in LABArola! Stai costruendo una serie di successi. La costanza premia."
        "labarola_10_wins" -> "10 vittorie! Sei un giocatore affermato di LABArola. La tua padronanza lessicale cresce."
        "labarola_20_wins" -> "20 vittorie! Un traguardo notevole che dimostra dedizione e competenza nel gioco."
        "labarola_streak_3" -> "Serie di 3 giorni! Hai indovinato la parola per 3 giorni consecutivi. La continuità è la chiave del successo."
        
        else -> "${achievement.description} Questo traguardo rappresenta un momento importante del tuo percorso accademico e dimostra il tuo impegno e la tua dedizione agli studi."
    }
}
