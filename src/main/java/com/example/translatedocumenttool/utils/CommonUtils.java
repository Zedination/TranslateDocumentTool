package com.example.translatedocumenttool.utils;

import com.example.translatedocumenttool.model.GroupCellData;
import com.example.translatedocumenttool.task.TranslateTask;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import java.io.IOException;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommonUtils {
    public static boolean isJapanese(String text) {
        // Biểu thức chính quy để kiểm tra ký tự tiếng Nhật
        String regex = "[\\p{InHiragana}\\p{InKatakana}\\p{InCJK_Unified_Ideographs}]+";

        // Tạo Pattern
        Pattern pattern = Pattern.compile(regex);

        // Tạo Matcher
        Matcher matcher = pattern.matcher(text);

        // Kiểm tra xem có khớp hay không
        return matcher.find();
    }

    public static void drawBorderRangeCell(Sheet sheet, int startRowIndex, int endRowIndex, int columnIndex) {
        // Chọn nhóm các ô cần đặt đường viền outline
        CellRangeAddress selectedRange = new CellRangeAddress(startRowIndex, endRowIndex, columnIndex, columnIndex);

        // Áp dụng CellStyle cho toàn bộ nhóm
        RegionUtil.setBorderTop(BorderStyle.THICK, selectedRange, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THICK, selectedRange, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THICK, selectedRange, sheet);
        RegionUtil.setBorderRight(BorderStyle.THICK, selectedRange, sheet);

        RegionUtil.setTopBorderColor(IndexedColors.BLUE.getIndex(), selectedRange, sheet);
        RegionUtil.setBottomBorderColor(IndexedColors.BLUE.getIndex(), selectedRange, sheet);
        RegionUtil.setLeftBorderColor(IndexedColors.BLUE.getIndex(), selectedRange, sheet);
        RegionUtil.setRightBorderColor(IndexedColors.BLUE.getIndex(), selectedRange, sheet);
    }

    public static String translate(String srcText, String srcLang, String targetLang, String transServerEndpoint) {
        var param = "?src_lang=" + encodeValue(srcLang) + "&src_text=" + encodeValue(srcText) + "&target_lang=" +  encodeValue(targetLang);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(transServerEndpoint + param))
                .GET()
                .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            return StringUtils.EMPTY;
        }
        if (response.statusCode() == 200) {
            return JsonParser.parseString(response.body()).getAsJsonObject().get("target_text").getAsString();
        } else {
            return StringUtils.EMPTY;
        }
    }

    private static String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String getCachedValueOfCell(Cell cell){
        DataFormatter formatter = new DataFormatter();
        if (cell.getCellType() == CellType.FORMULA) {
            switch (cell.getCachedFormulaResultType()) {
                case BOOLEAN -> {
                    return String.valueOf(cell.getBooleanCellValue());
                }
                case NUMERIC -> {
                    DecimalFormat df = new DecimalFormat("#.##");
                    df.setRoundingMode(RoundingMode.HALF_UP);
                    return String.valueOf(df.format(cell.getNumericCellValue()));
                }
                case STRING -> {
                    return cell.getStringCellValue();
                }
            }
        }
        return formatter.formatCellValue(cell);

    }

    public static void translateCellByCell(Workbook workbook, List<String> sheetsList, String srcLang, String targetLang, String endpoint, long count, AtomicLong progress, TranslateTask task) {
        sheetsList.forEach(sheetName -> {
            Sheet sheet = workbook.getSheet(sheetName);
            var rowIter = sheet.rowIterator();
            while (rowIter.hasNext()) {
                var row = rowIter.next();
                var cellIter = row.cellIterator();
                while (cellIter.hasNext()) {
                    var cell = cellIter.next();
                    if (CellType.STRING.equals(cell.getCellType()) || CellType.FORMULA.equals(cell.getCellType())) {
                        String srcText = getStringValueFromCell(workbook, cell);
                        if (StringUtils.trimToEmpty(srcText).equals(StringUtils.EMPTY)) continue;
                        String translationText = null;
                        try {
                            translationText = CommonUtils.translate(srcText,srcLang, targetLang, endpoint);
                        } catch (Exception e) {
                            progress.addAndGet(1);
                            task.updateProgress(progress.get(), count);
                            continue;
                        }
                        if (Objects.nonNull(translationText)) cell.setCellValue(translationText);
                    }
                    progress.addAndGet(1);
                    task.updateProgress(progress.get(), count);
                }
            }
        });
    }

    public static void translateGroupByCellSameColumn(Workbook workbook, List<String> sheetsList, String srcLang, String targetLang, String endpoint, long count, AtomicLong progress, TranslateTask task) {
        sheetsList.forEach(sheetName -> {
            Sheet sheet = workbook.getSheet(sheetName);
            var rowIter = sheet.rowIterator();
            Map<Integer, GroupCellData> mapCell = new HashMap<>();
            while (rowIter.hasNext()) {
                var row = rowIter.next();
                var cellIter = row.cellIterator();
                while (cellIter.hasNext()) {
                    var cell = cellIter.next();
                    var celVal = getStringValueFromCell(workbook, cell);
                    if (StringUtils.isBlank(celVal)) {
                        progress.addAndGet(1);
                        task.updateProgress(progress.get(), count);
                        continue;
                    }
                    GroupCellData groupCellData = mapCell.getOrDefault(cell.getColumnIndex(), new GroupCellData(row.getRowNum(), new ArrayList<>()));
                    groupCellData.getTargetTexts().add(celVal);
                    mapCell.put(cell.getColumnIndex(), groupCellData);
                    var bellowCellVal = Optional.ofNullable(sheet.getRow(row.getRowNum() + 1)).map(r -> r.getCell(cell.getColumnIndex()))
                            .map(c -> getStringValueFromCell(workbook, c)).orElse(StringUtils.EMPTY);

                    if (StringUtils.isBlank(bellowCellVal)) {
                        // start translate
                        String output = groupCellData.processText().stream().map(s -> CommonUtils.translate(s, srcLang, targetLang, endpoint))
                                .collect(Collectors.joining("\n"));
                        addComment(workbook, sheet, sheet.getRow(groupCellData.getFirstDataIndex()), sheet.getRow(groupCellData.getFirstDataIndex()).getCell(cell.getColumnIndex()), "Tool dịch AI" ,output);
                        CommonUtils.drawBorderRangeCell(sheet, groupCellData.getFirstDataIndex(), row.getRowNum(), cell.getColumnIndex());
                        mapCell.remove(cell.getColumnIndex());
                    }
                    progress.addAndGet(1);
                    task.updateProgress(progress.get(), count);
                }
            }
        });
    }



    public static void addComment(Workbook workbook, Sheet sheet, Row row, Cell cell, String author, String commentText) {
        CreationHelper factory = workbook.getCreationHelper();
        //get an existing cell or create it otherwise:

        ClientAnchor anchor = factory.createClientAnchor();
        //i found it useful to show the comment box at the bottom right corner
        anchor.setCol1(cell.getColumnIndex() + 1); //the box of the comment starts at this given column...
        anchor.setCol2(cell.getColumnIndex() + 8); //...and ends at that given column
        anchor.setRow1(row.getRowNum() + 1); //one row below the cell...
        anchor.setRow2(row.getRowNum() + 12); //...and 15 rows high

        Drawing drawing = sheet.createDrawingPatriarch();
        Comment comment = drawing.createCellComment(anchor);
        //set the comment text and author
        comment.setString(factory.createRichTextString(commentText));
        comment.setAuthor(author);

        cell.setCellComment(comment);
    }

    public static String getStringValueFromCell(Workbook workbook, Cell cell) {
        DataFormatter dataFormatter = new DataFormatter();
        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        try {
            return dataFormatter.formatCellValue(cell, formulaEvaluator);
        }catch (Exception e){
            return CommonUtils.getCachedValueOfCell(cell);
        }
    }

    public static boolean isValidURL(String urlString) {
        try {
            URL url = new URL(urlString);
            // Nếu không có ngoại lệ nào được ném, URL là hợp lệ
            return true;
        } catch (MalformedURLException e) {
            // Nếu có ngoại lệ MalformedURLException, URL không hợp lệ
            return false;
        }
    }

    public static String LIST_LANG = """
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
}
