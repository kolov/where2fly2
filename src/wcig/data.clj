(ns wcig.data
  (:require
    [clj-time.core :as t]
    [wcig.util :refer :all]
    )
  (:import (org.joda.time DateTimeZone)))

(def europe-airports [
                      "AJA"                                 ; ajaccio
                      "ACE"                                 ;
                      "ADB"                                 ; lanzarote
                      "AGP"                                 ; izmir
                      "ALC"                                 ; Malaga
                      "AMS"                                 ; valencia
                      "ARN"
                      "ATH"                                 ; stockholm
                      "AVN"                                 ; athens
                      "BCN"                                 ; avignon
                      ; "AYT"                                 ;antalya
                      ; "BJL"  ; banjul
                      ;  "BOJ"                               ; burgas
                      "BES"                                 ; barcelona
                      "BIO"                                 ; brest
                      "BLQ"                                 ; bilbao
                      "BSL"                                 ; boogna
                      ; "BLL"                               ; billund
                      ;"BRI" ; Bari
                      "BUD"                                 ; Basel
                      "CMF"                                 ; budapest
                      ; "CIA"
                      ;  "CDG"
                      ; "CEQ"                                 ; cannes
                      ;   "CFU"                               ; Kerkyra
                      "CTA"                                 ; chambery
                      "DBV"                                 ; Catania
                      ; "EGC"
                      "DXB"                                 ; dubrovnik
                      "DUB"                                 ; dubai
                      ;  "DIJ"                                 ; dijon
                      "DXB"                                 ; dublin
                      "EDI"                                 ; edinbourg
                      ;"ESU"
                      "FAO"                                 ;faro
                      "FCO"                                 ;fiumicino
                      ; "FDH"                    ; friedrichshaven
                      "FNC"                                 ; funchal
                      "FUE"                                 ; fuerteventura
                      "GNB"                                 ; grenoble
                      "GOA"                                 ; genova
                      "GRO"                                 ;girona
                      "GVA"                                 ; geneve
                      ; "HEL"                               ; helsinki
                      ; "HER" ' heraklion
                      "IBZ"
                      "INN"
                      ; "JMK" mykonos
                      ; "JTR" thira
                      ; "KGS"                                 ; kos
                      "KUN"                                 ; Krakow
                      "KRK"                                 ; Krakow
                      ; "LCA"                                 ; Larnaca
                      "LCY"                                 ;London City
                      "LIN"                                 ; milano linate
                      "LIS"
                      "LHR"
                      ; "LPA"                                 ; las palmas
                      ;"LWG"
                      ;"LTN"
                      "MAD"                                 ; madrid
                      ; "MLA"                               ; malta
                      "MPL"                                 ; montpellier
                      "MUC"
                      "MRS"                                 ; marseille
                      "MXP"                                 ; milano
                      "NAP"
                      ; "NBE" enfidha
                      "NCE"
                      "NTE"                                 ; nantes
                      "ORK"                                 ;cork
                      "OSL"
                      "OPO"                                 ; Porto
                      "ORY"
                      ;"PMI"                                 ; palma de mallorca
                      "PGF"                                 ; perpignan
                      "PRG"
                      "PSA"                                 ;pisa
                      ; "PXO"
                      ;  "RHE"                                 ; reims
                      "SOF"
                      "SPU"                                 ; split
                      "SVQ"                                 ; sevilla
                      "SZG"                                 ; salzburg
                      ;"SOU"
                      ;"STN"

                      ; "TFS"                                 ;tenerife
                      "TLL"                                 ;talinn
                      "TXL"                                 ; tegel
                      ; "TLV"
                      "TRN"
                      ;"TUN"
                      "VCE"                                 ; venice
                      "VIE"                                 ; vienna
                      "VLC"                                 ; valencia
                      ; "VRN"                               ; varna
                      ;         "VXE"
                      "WAW"                                 ; warsaw
                      "ZRH"])



(def holiday-airports [
                       "AUA"                                ; aruba
                       ;   "BON"                                ; bonaire
                       "BKK"                                ; bangkok
                       "CUR"                                ; curacao
                       "DAD"                                ; Denpasar
                       "HAN"                                ; hanoi
                       "DPS"                                ; bali
                       "HKG"                                ; hong kong
                       ;    "NYC"
                       ;        "MIA"                                ; miami
                       "MLE"                                ; Male
                       "MRU"                                ; mauritsius
                       "PEK"                                ; beijing
                       "PUJ"                                ; punta cana
                       "PVG"                                ; shanghai
                       "SGN"                                ; ho chi min
                       ; madeira
                       "RIO"                                ; rio
                       ; kho thau
                       ; ko samui
                       "SEZ"                                ; seychelles
                       ;  "SXM"                                ; st maarten
                       ;        "TAB"                                ; tobago
                       "TFS"                                ; tenerife
                       ; "USM"                                ; kho samui
                       ])

(def all-airports (concat europe-airports holiday-airports))

(def tzAmsterdam (DateTimeZone/forID "Europe/Amsterdam"))

(def holidays [

              {
                :id         "WIN1" :name "Wintervakantie"
                :start-date "2017-2-18" :end-date "2017-2-25"
                :groups     #{"transavia" "holiday"}
                },
{
:id "PAA1" :name "Pasen"
 :start-date "2017-04-14" :end-date "2017-04-17"
:groups     #{"transavia" "holiday"}
}


              {
                :id         "MEI" :name "Meivakantie"
                :start-date "2017-4-22" :end-date "2017-04-30"
                :groups     #{"transavia" "holiday"}
                },
{ :id "PIN" :name "Pinksteren"
:start-date "2017-06-03" end-date "2017-06-05"
 :groups     #{"transavia" "holiday"}
                },
{ :id "ZOM1" :name "Zomervakantiei 1"
:start-date "2017-07-22" :end-date "2017-08-05"
                :groups     #{"transavia" "holiday"}
                },
{ :id "ZOM2" :name "Zomervakantie 2"
:start-date "2017-08-05" :end-date "2017-08-19"
                :groups     #{"transavia" "holiday"}
                },

{ :id "ZO32" :name "Zomervakantie 3"
:start-date "2017-08-19" :end-date "2017-09-02"
                :groups     #{"transavia" "holiday"}
                },

               ])





