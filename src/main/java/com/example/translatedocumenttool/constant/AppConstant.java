package com.example.translatedocumenttool.constant;

import java.util.HashMap;
import java.util.Map;

public class AppConstant {

    private static String LIST_LANG_NLLB = """
            Tiếng Aceh (chữ Ả Rập) - ace_Arab
            Tiếng Aceh (chữ Latinh) - ace_Latn
            Tiếng Ả Rập Lưỡng Hà - acm_Arab
            Tiếng Ả Rập Ta'izzi-Adeni - acq_Arab
            Tiếng Ả Rập Tunisia - aeb_Arab
            Afrikaans - afr_Latn
            Tiếng Ả Rập Nam Levantine - ajp_Arab
            Tiếng Akan - aka_Latn
            Tiếng Amhara - amh_Ethi
            Tiếng Ả Rập Bắc Levantine - apc_Arab
            Tiếng Ả Rập tiêu chuẩn hiện đại - arb_Arab
            Tiếng Ả Rập tiêu chuẩn hiện đại (La tinh hóa) - arb_Latn
            Tiếng Ả Rập Najdi - ars_Arab
            Tiếng Ả Rập Ma-rốc - ary_Arab
            Tiếng Ả Rập Ai Cập - arz_Arab
            Tiếng Assam - asm_Beng
            Tiếng Asturian - ast_Latn
            Tiếng Awadhi - awa_Deva
            Tiếng Trung Aymara - ayr_Latn
            Nam Azerbaijan - azb_Arab
            Bắc Azerbaijan - azj_Latn
            Bashkir - bak_Cyrl
            Bambara - bam_Latn
            Tiếng Bali - ban_Latn
            Tiếng Belarus - bel_Cyrl
            Bemba - bem_Latn
            Tiếng Bengali - ben_Beng
            Tiếng Bhojpuri - bho_Deva
            Tiếng Banjar (chữ Ả Rập) - bjn_Arab
            Tiếng Banjar (chữ Latinh) - bjn_Latn
            Tiếng Tây Tạng tiêu chuẩn - bod_Tibt
            Tiếng Bosnia - bos_Latn
            tiếng Bugin - bug_Latn
            Tiếng Bun-ga-ri - bul_Cyrl
            Tiếng Catalan - cat_Latn
            Tiếng Cebuano - ceb_Latn
            Tiếng Séc - ces_Latn
            Tiếng Chokwe - cjk_Latn
            Tiếng Kurd miền Trung - ckb_Arab
            Tiếng Tatar Crimea - crh_Latn
            Tiếng Wales - cym_Latn
            Tiếng Đan-mạch - dan_Latn
            Tiếng Đức - deu_Latn
            Tiếng Dinka - dik_Latn
            Tiếng Dyula - dyu_Latn
            Tiếng Dzongkha - dzo_Tibt
            Tiếng Hy Lạp - ell_Grek
            Tiếng Anh - eng_Latn
            Tiếng Esperanto - epo_Latn
            Tiếng Estonia - est_Latn
            Tiếng Basque - eus_Latn
            Tiếng Ewe - ewe_Latn
            Tiếng Faroe - fao_Latn
            Tiếng Fiji - fij_Latn
            Tiếng Phần-lan - fin_Latn
            Tiếng Fon - fon_Latn
            Tiếng Pháp - fra_Latn
            Tiếng Friuli - fur_Latn
            Tiếng Fulfulde Nigeria - fuv_Latn
            Tiếng Gaelic Scotland - gla_Latn
            Tiếng Ireland - gle_Latn
            Tiếng Galicia - glg_Latn
            Tiếng Guaraní Paraguay - grn_Latn
            Tiếng Gujarat - guj_Gujr
            Tiếng Creole Haiti - hat_Latn
            Tiếng Hausa - hau_Latn
            Tiếng Do Thái - heb_Hebr
            Tiếng Hindi - hin_Deva
            Tiếng Chhattisgarhi - hne_Deva
            Tiếng Croatia - hrv_Latn
            Tiếng Hungary - hun_Latn
            Tiếng Armenia - hye_Armn
            Tiếng Igbo - ibo_Latn
            Tiếng Ilocano - ilo_Latn
            Tiếng Indonesia - ind_Latn
            Tiếng Iceland - isl_Latn
            Tiếng Ý - ita_Latn
            Tiếng Java - jav_Latn
            Tiếng Nhật - jpn_Jpan
            Tiếng Kabyle - kab_Latn
            Tiếng Jingpho (Tĩnh Phố) - kac_Latn
            Tiếng Kamba - kam_Latn
            Tiếng Kannada - kan_Knda
            Tiếng Kashmiri (chữ Ả Rập) - kas_Arab
            Tiếng Kashmiri (chữ Devanagari) - kas_Deva
            Tiếng Gruzia - kat_Geor
            Tiếng Trung Kanuri (chữ Ả Rập) - knc_Arab
            Tiếng Trung Kanuri (chữ Latinh) - knc_Latn
            Tiếng Kazakh - kaz_Cyrl
            Tiếng Kabiye - kbp_Latn
            Tiếng Kabuverdianu - kea_Latn
            Tiếng Khơ-me (Khmer) - khm_Khmr
            Tiếng Kikuyu - kik_Latn
            Tiếng Kinyarwanda - kin_Latn
            Tiếng Kyrgyz - kir_Cyrl
            Tiếng Kimbundu - kmb_Latn
            Tiếng Bắc Kurd - kmr_Latn
            Tiếng Kikongo - kon_Latn
            Tiếng Hàn - kor_Hang
            Tiếng Lào - lao_Laoo
            Tiếng Ligurian - lij_Latn
            Tiếng Limburgish - lim_Latn
            Tiếng Lingala - lin_Latn
            Tiếng Litva - lit_Latn
            Tiếng Lombard - lmo_Latn
            Tiếng Latgalian - ltg_Latn
            Tiếng Luxembourg - ltz_Latn
            Tiếng Luba-Kasai - lua_Latn
            Tiếng Ganda - lug_Latn
            Tiếng Luo - luo_Latn
            Tiếng Mizo - lus_Latn
            Tiếng Tiêu chuẩn Latvian - lvs_Latn
            Tiếng Magaha - mag_Deva
            Tiếng Maithil - mai_Deva
            Tiếng Malayalam - mal_Mlym
            Tiếng Marathi - mar_Deva
            Tiếng Minangkabau (kịch bản tiếng Ả Rập) - min_Arab
            Tiếng Minangkabau (kịch bản tiếng Latin) - min_Latn
            Tiếng Macedonia - mkd_Cyrl
            Plateau Malagasy - plt_Latn
            Tiếng Maltese - mlt_Latn
            Tiếng Meitei (Bengali script) - mni_Beng
            Tiếng Mông Cổ Halh - khk_Cyrl
            Tiếng Mossi - mos_Latn
            Tiếng Maori - mri_Latn
            Tiếng Myanmar - mya_Mymr
            Tiếng Hà Lan - nld_Latn
            Tiếng Na Uy Nynorsk - nno_Latn
            Tiếng Na Uy Bokmål - nob_Latn
            Tiếng Nepal - npi_Deva
            Tiếng Bắc Soto - nso_Latn
            Tiếng Nuer - nus_Latn
            Tiếng Nyanja - nya_Latn
            Tiếng Occitan - oci_Latn
            Tiếng West Central Oromo - gaz_Latn
            Tiếng Odia - ory_Orya
            Tiếng Pangasinan - pag_Latn
            Tiếng Đông Panjabi - pan_Guru
            Tiếng Papiamento - pap_Latn
            Tiếng Tây Ba Tư - pes_Arab
            Tiếng Ba Lan - pol_Latn
            Tiếng Bồ Đào Nha - por_Latn
            Tiếng Dari - prs_Arab
            Tiếng Nam Pashto - pbt_Arab
            Tiếng Ayacucho Quechua - quy_Latn
            Tiếng Rumani - ron_Latn
            Tiếng RUNDI - run_Latn
            Tiếng Nga - rus_Cyrl
            Tiếng Sango - sag_Latn
            Tiếng Phạn - san_Deva
            Tiếng Santali - sat_Olck
            Tiếng Sicilian - scn_Latn
            Tiếng Shan - shn_Mymr
            Tiếng Sinhala - sin_Sinh
            Tiếng Slovak - slk_Latn
            Tiếng Slovenia - slv_Latn
            Tiếng Samoa - smo_Latn
            Tiếng Shona - sna_Latn
            Tiếng Sindhi - snd_Arab
            Tiếng Somali - som_Latn
            Tiếng Nam Soto - sot_Latn
            Tiếng Tây Ban Nha - spa_Latn
            Tiếng Tosk Albania - als_Latn
            Tiếng Sardinia - srd_Latn
            Tiếng Serbia - srp_Cyrl
            Tiếng Swati - ssw_Latn
            Tiếng Sundan - sun_Latn
            Tiếng Thụy Điển - swe_Latn
            Tiếng Swahili - swh_Latn
            Tiếng Silesian - szl_Latn
            Tiếng Tamil - tam_Taml
            Tiếng Tatar - tat_Cyrl
            Tiếng Telugu - tel_Telu
            Tiếng Tajik - tgk_Cyrl
            Tiếng Tagalog - tgl_Latn
            Tiếng Thái - tha_Thai
            Tigrinya - tir_Ethi
            Tiếng Tamasheq (Latin script) - taq_Latn
            Tiếng Tamasheq (Tifinagh script) - taq_Tfng
            Tiếng Tok Pisin - tpi_Latn
            Tiếng Tswana - tsn_Latn
            Tiếng Tsonga - tso_Latn
            Tiếng Turkmen - tuk_Latn
            Tiếng Tumbuka - tum_Latn
            Tiếng Thổ Nhĩ Kỳ - tur_Latn
            Tiếng Twi - twi_Latn
            Tiếng Central Atlas Tamazight - tzm_Tfng
            Tiếng Uyghur - uig_Arab
            Tiếng Ukraine - ukr_Cyrl
            Tiếng Umbundu - umb_Latn
            Tiếng Urdu - urd_Arab
            Tiếng Bắc Uzbek - uzn_Latn
            Tiếng Venice - vec_Latn
            Tiếng Việt - vie_Latn
            Tiếng Waray - war_Latn
            Tiếng Wolof - wol_Latn
            Tiếng Xhosa - xho_Latn
            Tiếng Đông Yiddish - ydd_Hebr
            Tiếng Yoruba - yor_Latn
            Tiếng Quảng Đông - yue_Hant
            Tiếng Trung Quốc (Giản thể) - zho_Hans
            Tiếng Trung Quốc (Phổn thể) - zho_Hant
            Tiếng Malay - zsm_Latn
            Tiếng Zulu - zul_Latn
            """;

    private static String LIST_LANG_MADLAD_400 = """
            English - en
            Russian - ru
            Spanish - es
            French - fr
            German - de
            Italian - it
            Portuguese - pt
            Polish - pl
            Dutch - nl
            Vietnamese - vi
            Turkish - tr
            Swedish - sv
            Indonesian - id
            Romanian - ro
            Czech - cs
            Mandarin Chinese - zh
            Hungarian - hu
            Japanese - ja
            Thai - th
            Finnish - fi
            Persian - fa
            Ukrainian - uk
            Danish - da
            Greek - el
            Norwegian - no
            Bulgarian - bg
            Slovak - sk
            Korean - ko
            Arabic - ar
            Lithuanian - lt
            Catalan - ca
            Slovenian - sl
            Hebrew - he
            Estonian - et
            Latvian - lv
            Hindi - hi
            Albanian - sq
            Malay - ms
            Azerbaijani - az
            Serbian - sr
            Tamil - ta
            Croatian - hr
            Kazakh - kk
            Icelandic - is
            Malayalam - ml
            Marathi - mr
            Telugu - te
            Afrikaans - af
            Galician - gl
            Filipino - fil
            Belarusian - be
            Macedonian - mk
            Basque - eu
            Bengali - bn
            Georgian - ka
            Mongolian - mn
            Bosnian - bs
            Uzbek - uz
            Urdu - ur
            Swahili - sw
            Cantonese - yue
            Nepali - ne
            Kannada - kn
            Kara-Kalpak - kaa
            Gujarati - gu
            Sinhala - si
            Welsh - cy
            Esperanto - eo
            Latin - la
            Armenian - hy
            Kyrghyz - ky
            Tajik - tg
            Irish - ga
            Maltese - mt
            Myanmar (Burmese) - my
            Khmer - km
            Tatar - tt
            Somali - so
            Kurdish (Kurmanji) - ku
            Pashto - ps
            Punjabi - pa
            Kinyarwanda - rw
            Lao - lo
            Hausa - ha
            Dhivehi - dv
            W. Frisian - fy
            Luxembourgish - lb
            Kurdish (Kurmanji) - ckb
            Malagasy - mg
            Scottish Gaelic - gd
            Amharic - am
            Uyghur - ug
            Haitian Creole - ht
            Ancient Greek - grc
            Hmong - hmn
            Sindhi - sd
            Javanese - jv
            Maori - mi
            Turkmen - tk
            Cebuano - ceb
            Yiddish - yi
            Bashkir - ba
            Faroese - fo
            Odia (Oriya) - or
            Xhosa - xh
            Sundanese - su
            Kalaallisut - kl
            Chichewa - ny
            Samoan - sm
            Shona - sn
            Corsican - co
            Zulu - zu
            Igbo - ig
            Yoruba - yo
            Papiamento - pap
            Sesotho - st
            Hawaiian - haw
            Assamese - as
            Occitan - oc
            Chuvash - cv
            Mizo - lus
            Tetum - tet
            Swiss German - gsw
            Yakut - sah
            Breton - br
            Romansh - rm
            Sanskrit - sa
            Tibetan - bo
            Oromo - om
            N. Sami - se
            Chechen - ce
            Hakha Chin - cnh
            Ilocano - ilo
            Hiligaynon - hil
            Udmurt - udm
            Ossetian - os
            Luganda - lg
            Tigrinya - ti
            Venetian - vec
            Tsonga - ts
            Tuvinian - tyv
            Kabardian - kbd
            Ewe - ee
            Iban - iba
            Avar - av
            Khasi - kha
            Tonga (Tonga Islands) - to
            Tswana - tn
            Sepedi - nso
            Fijian - fj
            Zaza - zza
            Twi - ak
            Adangme - ada
            Querétaro Otomi - otq
            Dzongkha - dz
            Buryat - bua
            Falam Chin - cfm
            Lingala - ln
            Meadow Mari - chm
            Guarani - gn
            Karachay-Balkar - krc
            Walloon - wa
            Fiji Hindi - hif
            Yucateco - yua
            Sranan Tongo - srn
            Waray (Philippines) - war
            Romani - rom
            Central Bikol - bik
            Pampanga - pam
            Sango - sg
            Luba-Katanga - lu
            Adyghe - ady
            Kabiyè - kbp
            Syriac - syr
            Latgalian - ltg
            Erzya - myv
            Isoko - iso
            Kachin - kac
            Bhojpuri - bho
            Aymara - ay
            Kumyk - kum
            Quechua - qu
            Zhuang - za
            Pangasinan - pag
            Guerrero Nahuatl - ngu
            Venda - ve
            Paite Chin - pck
            Zapotec - zap
            Tày - tyz
            Huli - hui
            Batak Toba - bbc
            Tzotzil - tzo
            Tiv - tiv
            Kuanua - ksd
            Goan Konkani - gom
            Minangkabau - min
            Old English - ang
            E. Huasteca Nahuatl - nhe
            E. Baluchi - bgp
            Nzima - nzi
            Nande - nnb
            Navajo - nv
            Noise - zxx
            Baoulé - bci
            Komi - kv
            Newari - new
            Dadibi - mps
            S. Altai - alt
            Motu - meu
            Betawi - bew
            Fon - fon
            Inuktitut - iu
            Ambulas - abt
            Makhuwa-Meetto - mgh
            Mon - mnw
            Tuvalu - tvl
            Dombe - dov
            Klingon - tlh
            Hiri Motu - ho
            Cornish - kw
            Hill Mari - mrj
            Kedah Malay - meo
            Crimean Tatar - crh
            Matigsalug Manobo - mbt
            N. Emberá - emp
            Achinese - ace
            Iu Mien - ium
            Mam - mam
            Ngäbere - gym
            Maithili - mai
            Seselwa Creole French - crs
            Pohnpeian - pon
            Umbu-Ungu - ubu
            Fipa - fip
            K’iche’ - quc
            Manx - gv
            Kuanyama - kj
            Batak Karo - btx
            Bukiyip - ape
            Chuukese - chk
            Réunion Creole French - rcf
            Shan - shn
            Tzeltal - tzh
            Moksha - mdf
            Uma - ppk
            Swati - ss
            Gagauz - gag
            Garifuna - cab
            Krio - kri
            Sena - seh
            Ibibio - ibb
            Ditammari - tbz
            E. Bru - bru
            Enga - enq
            Acoli - ach
            San Blas Kuna - cuk
            Kimbundu - kmb
            Wolof - wo
            Kekchí - kek
            Huallaga Huánuco Quechua - qub
            Tabassaran - tab
            Batak Simalungun - bts
            Kosraean - kos
            Rawa - rwo
            Kaqchikel - cak
            Mutu - tuc
            Bulu - bum
            Chokwe - cjk
            Gilbertese - gil
            Saterfriesisch - stq
            Tausug - tsg
            S. Bolivian Quechua - quh
            Makasar - mak
            Mapudungun - arn
            Balinese - ban
            Shuar - jiv
            Epena - sja
            Yapese - yap
            Tulu - tcy
            Tojolabal - toj
            Termanu - twu
            Kalmyk - xal
            Guerrero Amuzgo - amu
            Carpathian Romani - rmc
            Huastec - hus
            Nias - nia
            Khakas - kjh
            Bambara - bm
            Guahibo - guh
            Masai - mas
            St Lucian Creole French - acf
            Kadazan Dusun - dtp
            S’gaw Karen - ksw
            Belize Kriol English - bzj
            Dinka - din
            Zande - zne
            Madurese - mad
            Sabah Malay - msi
            Magahi - mag
            Kupang Malay - mkn
            Kongo - kg
            Lahu - lhu
            Chamorro - ch
            Imbabura H. Quichua - qvi
            Marshallese - mh
            E. Maroon Creole - djk
            Susu - sus
            Morisien - mfe
            Saramaccan - srm
            Dyula - dyu
            Chol - ctu
            E. Bolivian Guaraní - gui
            Palauan - pau
            Inga - inb
            Bislama - bi
            Meiteilon (Manipuri) - mni
            Wayuu - guc
            Jamaican Creole English - jam
            Wolaytta - wal
            Popti’ - jac
            Basa (Cameroon) - bas
            Gorontalo - gor
            Saraiki - skr
            Nyungwe - nyu
            Woun Meu - noa
            Toraja-Sa’dan - sda
            Guajajára - gub
            Nogai - nog
            Asháninka - cni
            Teso - teo
            Tandroy-Mahafaly Malagasy - tdx
            Sangir - sxn
            Rakhine - rki
            South Ndebele - nr
            Arpitan - frp
            Alur - alz
            E. Tamang - taj
            N. Luri - lrc
            Chopi - cce
            Rundi - rn
            Caribbean Javanese - jvn
            Sabu - hvn
            Ngaju - nij
            Dawro - dwr
            Izii - izz
            Agusan Manobo - msm
            Bokobaru - bus
            Kituba (DRC) - ktu
            Cherokee - chr
            Central Mazahua - maz
            Tz’utujil - tzj
            Sunwar - suz
            W. Kanjobal - knj
            Bimoba - bim
            Gulay - gvl
            Boko (Benin) - bqc
            Ticuna - tca
            Pijin - pis
            Parauk - prk
            Lango (Uganda) - laj
            Central Melanau - mel
            Cañar H. Quichua - qxr
            Nandi - niq
            Akha - ahk
            Shipibo-Conibo - shp
            Chhattisgarhi - hne
            Supyire Senoufo - spp
            Komi-Permyak - koi
            Kinaray-A - krj
            Lambayeque Quechua - quf
            S. Luri - luz
            Aguaruna - agr
            Tswa - tsc
            Manggarai - mqy
            Gofa - gof
             - 20
            Garhwali - gbm
            Mískito - miq
            Zarma - dje
            Awadhi - awa
            Kanauji - bjj
            N. Pastaza Quichua - qvz
            Surjapuri - sjp
            Tetela - tll
            Rajasthani - raj
            Khmu - kjg
            Banggai - bgz
            Ayacucho Quechua - quy
            Chavacano - cbk
            Batak Angkola - akb
            Ojibwa - oj
            Keley-I Kallahan - ify
            Hassaniyya - mey
            Kashmiri - ks
            Chuj - cac
            Bodo (India) - brx
            S. Pastaza Quechua - qup
            Sylheti - syl
            Jambi Malay - jax
            Fulfulde - ff
            Tamazight (Tfng) - ber
            Takestani - tks
            Kok Borok - trp
            Maranao - mrw
            Adhola - adh
            Simte - smt
            Serer - srr
            Maasina Fulfulde - ffm
            Cajamarca Quechua - qvc
            Mewari - mtr
            Obolo - ann
            Kara-Kalpak (Latn) - kaa-Latn
            Afar - aa
            Nimadi - noe
            Nung (Viet Nam) - nut
            Guyanese Creole English - gyn
            Awa-Cuaiquer - kwi
            Manado Malay - xmm
            Masbatenyo - msb
            """;

    public static Map<String, String> MAP_LANGS = new HashMap<>() {{
        put("MADLAD-400-MT", LIST_LANG_MADLAD_400);
        put("NLLB-200", LIST_LANG_NLLB);
    }};
}
