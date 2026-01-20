package com.laba.firenze.domain.model

import java.util.UUID

enum class PartnerCategory(val displayName: String) {
    TIPOGRAFIA("Tipografia / Copisteria"),
    CARTOLERIA("Cartoleria / Belle Arti"),
    MERCERIA("Merceria"),
    BOOKSTORE("Bookstore"),
    FASHION("Abbigliamento"),
    FOOD("Ristoro"),
    FITNESS("Fitness"),
    SERVIZI("Servizi");

    companion object {
        fun allCases(): List<PartnerCategory> = values().toList()
    }
}

data class StudentBenefitPartner(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: PartnerCategory,
    val address: String,
    val phone: String?,
    val extraInfo: String?,
    val mapLink: String?
) {
    val highlight: String?
        get() {
            if (extraInfo.isNullOrBlank()) return null
            val info = extraInfo.trim()
            val firstBreak = info.lines().firstOrNull { it.isNotBlank() }
            if (firstBreak != null) return firstBreak.trim()
            val dotIndex = info.indexOf(".")
            if (dotIndex != -1) {
                return info.substring(0, dotIndex + 1).trim()
            }
            return info
        }

    val additionalNotes: String?
        get() {
            if (extraInfo.isNullOrBlank()) return null
            val info = extraInfo.trim()
            val lines = info.lines().filter { it.isNotBlank() }
            if (lines.size > 1) {
                return lines.drop(1).joinToString("\n").trim()
            }
            val dotIndex = info.indexOf(".")
            if (dotIndex != -1 && dotIndex < info.length - 1) {
                val rest = info.substring(dotIndex + 1).trim()
                return if (rest.isNotEmpty()) rest else null
            }
            return null
        }

    companion object {
        val samples = listOf(
            StudentBenefitPartner(
                name = "Ad Futura",
                category = PartnerCategory.TIPOGRAFIA,
                address = "Via Kyoto, 18, 50126 Firenze",
                phone = "0556813218",
                extraInfo = "1 tesi su 4 omaggio; 10% sconto su stampe per studenti e supporto per la stampa.",
                mapLink = "https://maps.google.com/?q=Via%20Kyoto,%2018,%2050126%20Firenze"
            ),
            StudentBenefitPartner(
                name = "Aviem Tessuti",
                category = PartnerCategory.MERCERIA,
                address = "Via G. di Vittorio 41, 59013 Montemurlo (PO)",
                phone = "0574655107",
                extraInfo = "Aperto agli studenti il sabato 8:30-12. Sconto studenti.",
                mapLink = null
            ),
            StudentBenefitPartner(
                name = "Bacci Tessuti",
                category = PartnerCategory.MERCERIA,
                address = "Via dell’Ariento 32R, 50123 Firenze",
                phone = "055216508",
                extraInfo = "10% sconto per studenti.",
                mapLink = "https://maps.google.com/?q=Via%20dell'Ariento%2032R,%2050123%20Firenze"
            ),
            StudentBenefitPartner(
                name = "Bar Agatha",
                category = PartnerCategory.FOOD,
                address = "Via dei Pescioni 5R, 50123 Firenze",
                phone = "0550192435",
                extraInfo = "1€ di sconto sul menù con acqua e caffè inclusi. Possibilità di studiare nel locale senza servizio al tavolo. Spritz a 5€.",
                mapLink = "https://maps.google.com/?q=Via%20dei%20Pescioni%205R,%2050123%20Firenze"
            ),
            StudentBenefitPartner(
                name = "Cartoleria Rigacci",
                category = PartnerCategory.CARTOLERIA,
                address = "Via de' Servi 71R, 50122 Firenze",
                phone = "055216206",
                extraInfo = "Range di 10-15% di sconto su vari articoli, inclusi alcuni già scontati.",
                mapLink = "https://maps.google.com/?q=Via%20dei%20Servi%2071R,%2050122%20Firenze"
            ),
            StudentBenefitPartner(
                name = "Cartoleria Salvini Belle Arti",
                category = PartnerCategory.TIPOGRAFIA,
                address = "Via degli Alfani 111R, 50121 Firenze",
                phone = "055219421",
                extraInfo = "10% sconto per studenti.",
                mapLink = "https://maps.google.com/?q=Via%20degli%20Alfani%20111R,%2050121%20Firenze"
            ),
            StudentBenefitPartner(
                name = "Fashion Room Bookstore",
                category = PartnerCategory.BOOKSTORE,
                address = "Via Il Prato 7r, 50123 Firenze",
                phone = "055213270",
                extraInfo = "10% sconto per studenti.",
                mapLink = "https://maps.google.com/?q=Via%20Il%20Prato%207R,%2050123%20Firenze"
            ),
            StudentBenefitPartner(
                name = "La Rinascente",
                category = PartnerCategory.FASHION,
                address = "Piazza della Repubblica, Firenze",
                phone = null,
                extraInfo = "10% sconto per studenti.",
                mapLink = "https://maps.google.com/?q=Piazza%20della%20Repubblica,%20Firenze"
            ),
            StudentBenefitPartner(
                name = "Mercato Centrale",
                category = PartnerCategory.FOOD,
                address = "Piazza del Mercato Centrale 4, 50123 Firenze",
                phone = null,
                extraInfo = "10% sconto per studenti.",
                mapLink = "https://maps.google.com/?q=Piazza%20del%20Mercato%20Centrale,%2050123%20Firenze"
            ),
            StudentBenefitPartner(
                name = "Merceria Delcor",
                category = PartnerCategory.MERCERIA,
                address = "Via della Spada 14r, 50123 Firenze",
                phone = "055212202",
                extraInfo = "10% sconto per studenti.",
                mapLink = "https://maps.google.com/?q=Via%20della%20Spada%2014R,%2050123%20Firenze"
            ),
            StudentBenefitPartner(
                name = "Obicà",
                category = PartnerCategory.FOOD,
                address = "Via de' Tornabuoni 16, 50123 Firenze",
                phone = "0552773526",
                extraInfo = "10% sconto sul menù alla carta; 15% sugli ordini take away; menù lunch a €14.",
                mapLink = "https://maps.google.com/?q=Via%20de'%20Tornabuoni%2016,%2050123%20Firenze"
            ),
            StudentBenefitPartner(
                name = "Palestra Relax Firenze",
                category = PartnerCategory.FITNESS,
                address = "Via degli Strozzi 2, 50123 Firenze",
                phone = "055284683",
                extraInfo = "Sconti su pacchetti massaggi, lezioni, open tutti i corsi e abbonamenti stagionali. Dettagli disponibili in reception.",
                mapLink = "https://maps.google.com/?q=Via%20degli%20Strozzi%202,%2050123%20Firenze"
            ),
            StudentBenefitPartner(
                name = "Pastation",
                category = PartnerCategory.FOOD,
                address = "Via Porta Rossa 64, 50123 Firenze",
                phone = "055291184",
                extraInfo = "15% di sconto per studenti; docenti con carta fedeltà.",
                mapLink = "https://maps.google.com/?q=Via%20Porta%20Rossa%2064,%2050123%20Firenze"
            ),
            StudentBenefitPartner(
                name = "Self Service Leonardo",
                category = PartnerCategory.FOOD,
                address = "Via dei Pecori 11, 50123 Firenze",
                phone = null,
                extraInfo = "10% sconto per studenti più acqua in brocca su richiesta.",
                mapLink = "https://maps.google.com/?q=Via%20dei%20Pecori%2011,%2050123%20Firenze"
            )
        )
    }
}
