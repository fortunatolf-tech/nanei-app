package com.example.data.provider

import com.example.data.model.DrugInfo
import com.example.data.model.LeapInfo
import com.example.data.model.Milestone
import com.example.data.model.RiskLevel

object NaneiStaticData {

    // --- 10 Saltos de Desenvolvimento Mental (Calculados pela Data Prevista do Parto) ---
    fun getMentalLeaps(): List<LeapInfo> = listOf(
        LeapInfo(
            leapNumber = 1,
            name = "Mundo das Sensações Mudadas",
            startWeek = 4,
            endWeek = 5,
            description = "O bebê percebe que o mundo ao seu redor mudou. Todos os sentidos (visão, audição, tato) se tornam mais nítidos simultaneamente.",
            fussySigns = listOf("Choro mais frequente", "Busca constante de colo", "Dificuldade para embalar o sono"),
            newAbilities = listOf("Primeiro sorriso voluntário", "Acompanha objetos com os olhos por alguns segundos", "Reage a ruídos conhecidos"),
            howToHelp = "Ofereça muito contato pele a pele, aconchego no sling e ambiente calmo com luz suave.",
            isStormyPhase = true
        ),
        LeapInfo(
            leapNumber = 2,
            name = "Mundo dos Padrões e Formas",
            startWeek = 7,
            endWeek = 9,
            description = "O bebê começa a reconhecer padrões no ambiente, em seu próprio corpo e nos movimentos das pessoas ao redor.",
            fussySigns = listOf("Estar mais agarrado à mãe", "Estranhar ruídos fortes", "Agitação ao mamar"),
            newAbilities = listOf("Observa as próprias mãos com fascínio", "Mexe pernas e braços ritmicamente", "Emite sons suaves (balbucios)"),
            howToHelp = "Mostre cartões com alto contraste (preto e branco) e cante melodias repetitivas.",
            isStormyPhase = true
        ),
        LeapInfo(
            leapNumber = 3,
            name = "Mundo das Transições Suaves",
            startWeek = 11,
            endWeek = 12,
            description = "Os movimentos abruptos dão lugar a transições mais fluidas. O bebê percebe variações de tom de voz e iluminação.",
            fussySigns = listOf("Sensibilidade aumentada a mudanças de ambiente", "Sonecas mais curtas"),
            newAbilities = listOf("Gira a cabeça acompanhando um objeto em movimento", "Mexe os braços com mais controle", "Sustenta a cabeça no tummy time"),
            howToHelp = "Balance suavemente o bebê e pratique posições confortáveis de bruços.",
            isStormyPhase = false
        ),
        LeapInfo(
            leapNumber = 4,
            name = "Mundo dos Eventos e Causas",
            startWeek = 18,
            endWeek = 19,
            description = "O salto das sequências e causa-efeito. O bebê entende que soltar um brinquedo faz ele cair ou produzir som.",
            fussySigns = listOf("Irritabilidade no fim da tarde", "Maior apego e recusa de colo de estranhos"),
            newAbilities = listOf("Alcança e agarra objetos intencionalmente", "Passa objetos de uma mão para a outra", "Grita de alegria e descobre a própria voz"),
            howToHelp = "Ofereça chocalhos leves, mordedores de texturas variadas e brinque de espelho.",
            isStormyPhase = true
        ),
        LeapInfo(
            leapNumber = 5,
            name = "Mundo das Relações e Distâncias",
            startWeek = 22,
            endWeek = 26,
            description = "O bebê compreende distâncias espaciais e percebe que os pais podem ir para outro cômodo (início da ansiedade de separação).",
            fussySigns = listOf("Choro ao ver os pais se afastando", "Recusa de sonecas no berço sozinhos"),
            newAbilities = listOf("Rola de bruços para costas", "Tenta alcançar objetos distantes", "Percebe a relação entre coisas (uma dentro da outra)"),
            howToHelp = "Brinque de 'Cadê o achou!' cobrindo o rosto para ensinar que você sempre volta.",
            isStormyPhase = true
        ),
        LeapInfo(
            leapNumber = 6,
            name = "Mundo das Categorias",
            startWeek = 33,
            endWeek = 37,
            description = "O bebê investiga tudo com minúcia, categorizando objetos por tamanho, textura, sabor e som.",
            fussySigns = listOf("Ansiedade com estranhos", "Alterações de apetite e sono noturno agitado"),
            newAbilities = listOf("Engatinha ou se arrasta com agilidade", "Examina pequenas sujeirinhas no chão", "Imita gestos simples como dar tchau"),
            howToHelp = "Permita exploração segura de potes de plástico, colheres de silicone e livros de pano.",
            isStormyPhase = true
        ),
        LeapInfo(
            leapNumber = 7,
            name = "Mundo das Sequências",
            startWeek = 42,
            endWeek = 46,
            description = "Compreensão de etapas para atingir um objetivo (ex.: pegar a colher -> colocar no prato -> levar à boca).",
            fussySigns = listOf("Teimosia evidente", "Exigência de atenção constante"),
            newAbilities = listOf("Tenta usar utensílios sozinho", "Aponta para o que deseja", "Empilha blocos simples"),
            howToHelp = "Encoraje a autonomia na introdução alimentar e elogie tentativas de montar brinquedos.",
            isStormyPhase = false
        ),
        LeapInfo(
            leapNumber = 8,
            name = "Mundo dos Programas e Rotinas",
            startWeek = 50,
            endWeek = 54,
            description = "Entendimento de rotinas inteiras: arrumar a mesa, preparar para o banho, vestir a roupa para sair.",
            fussySigns = listOf("Resistência na hora de vestir roupa ou trocar fralda", "Apegos emotivos a paninhos"),
            newAbilities = listOf("Dá os primeiros passos sem apoio", "Fala as primeiras palavras com significado ('mama', 'dada')", "Ajuda a guardar brinquedos"),
            howToHelp = "Mantenha uma rotina visual e previsível. Avise os passos da rotina com antecedência.",
            isStormyPhase = true
        ),
        LeapInfo(
            leapNumber = 9,
            name = "Mundo dos Princípios e Escolhas",
            startWeek = 60,
            endWeek = 64,
            description = "Início do pensamento estratégico: o bebê testa regras, negocia, imita papeis sociais e expressa preferências marcantes.",
            fussySigns = listOf("Birras e frustração intensa quando contrariado"),
            newAbilities = listOf("Faz charminhos e piadas", "Expressa empatia (oferece carinho)", "Entende limites simples"),
            howToHelp = "Dê escolhas limitadas (ex.: 'Quer a camiseta azul ou a vermelha?'), ajudando no sentimento de controle.",
            isStormyPhase = true
        ),
        LeapInfo(
            leapNumber = 10,
            name = "Mundo dos Sistemas e Consciência",
            startWeek = 71,
            endWeek = 75,
            description = "Compreensão de que ele é um indivíduo separado no sistema familiar, com vontade própria e capacidade de cooperar.",
            fussySigns = listOf("Oscilação entre querer ser bebê e querer ser independente"),
            newAbilities = listOf("Compreende histórias curtas", "Corre e pula com dois pés", "Expressa uma ampla gama de emoções"),
            howToHelp = "Incentive jogos colaborativos, leitura diária interativa e elogios aos esforços do pequeno.",
            isStormyPhase = false
        )
    )

    // --- Catálogo de Medicamentos na Lactação (Gateway com Fontes Oficiais) ---
    fun getDrugDatabase(): List<DrugInfo> = listOf(
        DrugInfo(
            id = "paracetamol",
            genericName = "Paracetamol (Acetaminofeno)",
            brandNames = listOf("Tylenol", "Paracetamol Genérico", "Dôflex", "Resfenol", "Cefaliv"),
            category = "Analgésico e Antitérmico",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Excretado no leite materno em quantidades insignificantes. Considerado analgésico de primeira escolha durante a amamentação pelas diretrizes internacionais e Ministério da Saúde.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/paracetamol/product/"
        ),
        DrugInfo(
            id = "ibuprofeno",
            genericName = "Ibuprofeno",
            brandNames = listOf("Advil", "Alivium", "Ibufran", "Motrin", "Spidufen", "Ibuprofeno Genérico"),
            category = "Anti-inflamatório Não Esteroidal (AINE)",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Níveis no leite materno são extremamente baixos e indetectáveis na maioria dos lactentes. Anti-inflamatório de escolha na lactação.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/ibuprofen/product/"
        ),
        DrugInfo(
            id = "dipirona",
            genericName = "Dipirona (Metamizol Sódico)",
            brandNames = listOf("Novalgina", "Anador", "Dipimed", "Neosaldina", "Lisador", "Maxalgina", "Atroveran"),
            category = "Analgésico e Antitérmico",
            riskLevel = RiskLevel.LOW,
            SummaryPt = "Amplamente utilizada no Brasil. Excretada no leite em baixas quantidades; uso eventual em doses terapêuticas é seguro, evitando-se tratamentos de longa duração.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/metamizole-sodium/product/"
        ),
        DrugInfo(
            id = "amoxicilina",
            genericName = "Amoxicilina",
            brandNames = listOf("Amoxil", "Velamox", "Novocilin", "Amoxicilina Genérica"),
            category = "Antibiótico Penicilínico",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Passa em pequena quantidade para o leite materno. Totalmente compatível com a amamentação. Observar apenas fezes amolecidas passageiras no bebê.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/amoxicillin/product/"
        ),
        DrugInfo(
            id = "amoxicilina_clavulanato",
            genericName = "Amoxicilina + Clavulanato de Potássio",
            brandNames = listOf("Clavulin", "Novamox", "Sinot Clav"),
            category = "Antibiótico de Largo Espectro",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Seguro durante a lactação. Excreção insignificante no leite. Pode causar leves alterações na flora intestinal do bebê.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/amoxicillin-clavulanic-acid/product/"
        ),
        DrugInfo(
            id = "loratadina",
            genericName = "Loratadina",
            brandNames = listOf("Claritin", "Loratamed", "Clarilerc", "Loratadina Genérico"),
            category = "Anti-histamínico (Antialérgico)",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Anti-histamínico de 2ª geração de primeira escolha na amamentação. Não causa sedação no lactente.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/loratadine/product/"
        ),
        DrugInfo(
            id = "desloratadina",
            genericName = "Desloratadina",
            brandNames = listOf("Desalex", "Sigmaliv", "Desloratadina Genérico"),
            category = "Anti-histamínico (Antialérgico)",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Composto ativo metabolizado da loratadina. Sem efeitos sedativos e com segurança confirmada na amamentação.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/desloratadine/product/"
        ),
        DrugInfo(
            id = "omeprazol",
            genericName = "Omeprazol",
            brandNames = listOf("Peprazol", "Losec", "Omeprazec", "Gastrocaps", "Omeprazol Genérico"),
            category = "Inibidor da Bomba de Prótons (Refluxo e Azia)",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "A quantidade excretada no leite é mínima e é rapidamente inativada pelo ácido gástrico do estômago do bebê. Uso seguro.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/omeprazole/product/"
        ),
        DrugInfo(
            id = "esomeprazol",
            genericName = "Esomeprazol / Pantoprazol",
            brandNames = listOf("Nexium", "Pantopraz", "Esogastro"),
            category = "Inibidor de Acidez e Gastrite",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Apresenta baixíssima transferência para o leite materno. Compatível com a amamentação.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/esomeprazole/product/"
        ),
        DrugInfo(
            id = "sertralina",
            genericName = "Sertralina",
            brandNames = listOf("Zoloft", "Assert", "Tolrest", "Serenata", "Sertralina Genérico"),
            category = "Antidepressivo (ISRS)",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Antidepressivo de escolha no pós-parto e lactação. Níveis no sangue do bebê são indetectáveis na imensa maioria dos casos.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/sertraline/product/"
        ),
        DrugInfo(
            id = "escitalopram",
            genericName = "Escitalopram",
            brandNames = listOf("Lexapro", "Reconter", "Exodus", "Escilex"),
            category = "Antidepressivo (ISRS)",
            riskLevel = RiskLevel.LOW,
            SummaryPt = "Pequena transferência para o leite materno. Geralmente seguro; monitorar sonolência ou irritabilidade incomum no bebê.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/escitalopram/product/"
        ),
        DrugInfo(
            id = "buscopan",
            genericName = "Escopolamina / Hioscina",
            brandNames = listOf("Buscopan", "Buscopan Composto", "Buscoduo"),
            category = "Antiespasmódico (Cólicas e Dores Abdominais)",
            riskLevel = RiskLevel.LOW,
            SummaryPt = "A esopolamina passa em quantidades irrisórias para o leite. Uso pontual para alívio de cólicas é compatível.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/scopolamine/product/"
        ),
        DrugInfo(
            id = "simeticona",
            genericName = "Simeticona (Dimeticona)",
            brandNames = listOf("Luftal", "Flagass", "Simeticona Genérico"),
            category = "Antigases e Anti-estufamento",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Não é absorvida pelo trato gastrointestinal da mãe, portanto não passa para o leite materno. Risco zero.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/simethicone/product/"
        ),
        DrugInfo(
            id = "domperidona",
            genericName = "Domperidona",
            brandNames = listOf("Motilium", "Peridal", "Domperix"),
            category = "Galactagogo e Antiemético",
            riskLevel = RiskLevel.LOW,
            SummaryPt = "Aumenta a prolactina e estimula a produção de leite materno. Níveis no leite são extremamente baixos. Deve ser usado com orientação médica.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/domperidone/product/"
        ),
        DrugInfo(
            id = "metoclopramida",
            genericName = "Metoclopramida",
            brandNames = listOf("Plasil", "Metoclopramida Genérico"),
            category = "Antiemético (Enjoos)",
            riskLevel = RiskLevel.LOW,
            SummaryPt = "Aumenta o fluxo de leite por estímulo de prolactina. Excretado no leite em pequena quantidade. Recomenda-se uso por curtos períodos.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/metoclopramide/product/"
        ),
        DrugInfo(
            id = "ondansetrona",
            genericName = "Ondansetrona",
            brandNames = listOf("Vonau Flash", "Nausedron", "Zofran"),
            category = "Antiemético e Náuseas",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Seguro para mães lactantes. Passa em níveis muito baixos no leite e não afeta o lactente.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/ondansetron/product/"
        ),
        DrugInfo(
            id = "desogestrel",
            genericName = "Desogestrel (Progestagênio Puro)",
            brandNames = listOf("Cerazette", "Kelly", "Nactali", "Amavel", "Nortrel"),
            category = "Anticoncepcional de Amamentação",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Pílula progestágena de escolha durante a amamentação. Não altera a produção, volume ou composição do leite materno.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/desogestrel/product/"
        ),
        DrugInfo(
            id = "anticoncepcional_combinado",
            genericName = "Etinilestradiol + Levonorgestrel (Combinados)",
            brandNames = listOf("Ciclo 21", "Selene", "Yasmin", "Adoless", "Microvlar"),
            category = "Anticoncepcional Oral Combinado",
            riskLevel = RiskLevel.MODERATE,
            SummaryPt = "O estrogênio pode reduzir significativamente a produção de leite materno, especialmente nos primeiros 6 meses. Prefira métodos com progestágeno isolado ou DIU.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/ethinylestradiol/product/"
        ),
        DrugInfo(
            id = "azitromicina",
            genericName = "Azitromicina",
            brandNames = listOf("Astro", "Azitromocin", "Clindal AZ", "Azitrex"),
            category = "Antibiótico Macrolídeo",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Excretada em pequenas quantidades no leite. Altamente compatível com o aleitamento materno.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/azithromycin/product/"
        ),
        DrugInfo(
            id = "cefalexina",
            genericName = "Cefalexina",
            brandNames = listOf("Keflex", "Cefalmed", "Cefalexina Genérica"),
            category = "Antibiótico Cefalosporina",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Excreção mínima no leite materno. Antibiótico seguro e de ampla utilização na amamentação.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/cephalexin/product/"
        ),
        DrugInfo(
            id = "sulfato_ferroso",
            genericName = "Sulfato Ferroso / Ferro Quelato",
            brandNames = listOf("Neutrofer", "Noripurum", "Anemifer", "Combiron"),
            category = "Suplemento Mineral e Antianêmico",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Essencial para reposição de ferro pós-parto. Não altera os níveis normais fisiológicos de ferro no leite, sendo 100% seguro.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/ferrous-sulfate/product/"
        ),
        DrugInfo(
            id = "vitamina_d",
            genericName = "Vitamina D3 (Colecalciferol)",
            brandNames = listOf("Depura", "Adtil", "Cronus D", "Sanasol"),
            category = "Suplemento Vitamínico",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Fundamental para a saúde óssea da mãe e do bebê. Doses de suplementação materna habitual são totalmente seguras.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/cholecalciferol/product/"
        ),
        DrugInfo(
            id = "nistatina",
            genericName = "Nistatina",
            brandNames = listOf("Nistatina Creme", "Nistatina Suspensão Oral"),
            category = "Antifúngico (Tratamento de Sapinho e Candidíase)",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Não é absorvida por via oral nem cutânea. Utilizada com segurança tanto na mama da mãe quanto na boca do bebê em caso de sapinho.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/nystatin/product/"
        ),
        DrugInfo(
            id = "nebacetin",
            genericName = "Neomicina + Bacitracina",
            brandNames = listOf("Nebacetin", "Cicatrigel", "Nebamin"),
            category = "Pomada Antibiótica Tópica",
            riskLevel = RiskLevel.VERY_LOW,
            SummaryPt = "Absorção sistêmica através da pele é insignificante. Seguro. Se aplicada nos mamilos, limpar bem antes de amamentar.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/neomycin/product/"
        ),
        DrugInfo(
            id = "nimesulida",
            genericName = "Nimesulida",
            brandNames = listOf("Scaflam", "Nisulid", "Cimelide", "Nimesulida Genérico"),
            category = "Anti-inflamatório",
            riskLevel = RiskLevel.MODERATE,
            SummaryPt = "Há dados limitados sobre excreção no leite. Recomenda-se priorizar alternativas com segurança mais bem documentada, como Ibuprofeno.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/nimesulide/product/"
        ),
        DrugInfo(
            id = "diclofenaco",
            genericName = "Diclofenaco de Sódio / Potássio",
            brandNames = listOf("Cataflam", "Voltaren", "Diclofenaco Genérico"),
            category = "Anti-inflamatório",
            riskLevel = RiskLevel.LOW,
            SummaryPt = "Baixa passagem para o leite materno. Seguro para tratamentos de curta duração.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/diclofenac/product/"
        ),
        DrugInfo(
            id = "dramin",
            genericName = "Dimenidrinato",
            brandNames = listOf("Dramin", "Dramin B6"),
            category = "Antiemético e Enjoo de Movimento",
            riskLevel = RiskLevel.LOW,
            SummaryPt = "Passa em pequenas quantidades para o leite. Uso eventual é compatível, devendo-se observar se o bebê apresenta sonolência.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/dimenhydrinate/product/"
        ),
        DrugInfo(
            id = "polaramine",
            genericName = "Dexclorfeniramina",
            brandNames = listOf("Polaramine", "Histamin", "Polaramine Genérico"),
            category = "Anti-histamínico Clássico",
            riskLevel = RiskLevel.LOW,
            SummaryPt = "Antialérgico de 1ª geração. Pode causar leve sedação. Dê preferência a anti-histamínicos de 2ª geração como Loratadina.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/dexchlorpheniramine/product/"
        ),
        DrugInfo(
            id = "pseudoefedrina",
            genericName = "Pseudoefedrina / Fenilefrina",
            brandNames = listOf("Tylenol Sinus", "Descongestionantes Orais", "Resfenol"),
            category = "Descongestionante Nasal Oral",
            riskLevel = RiskLevel.MODERATE,
            SummaryPt = "Pode reduzir a produção de leite materno (prolactina) e causar irritabilidade/insônia no lactente. Dar preferência a lavagem nasal com soro fisiológico.",
            lactMedUrl = "https://www.ncbi.nlm.nih.gov/books/NBK501922/",
            eLactanciaUrl = "https://e-lactancia.org/breastfeeding/pseudoephedrine/product/"
        )
    )

    // --- Marcos de Desenvolvimento Padrão OMS / CDC ---
    fun getStandardMilestones(babyId: Long): List<Milestone> = listOf(
        // Motor Grosso
        Milestone(babyId = babyId, category = "MOTOR_GROSS", title = "Sustenta a cabeça", description = "Consegue manter a cabeça erguida por alguns momentos no tummy time", targetAgeMonths = 2),
        Milestone(babyId = babyId, category = "MOTOR_GROSS", title = "Rola de bruços para costas", description = "Consegue virar o corpo da posição de barriga para baixo", targetAgeMonths = 4),
        Milestone(babyId = babyId, category = "MOTOR_GROSS", title = "Senta sem apoio", description = "Consegue permanecer sentado com equilíbrio por alguns minutos", targetAgeMonths = 6),
        Milestone(babyId = babyId, category = "MOTOR_GROSS", title = "Engatinha ou se arrasta", description = "Desloca-se pelo chão usando braços e pernas de forma coordenada", targetAgeMonths = 9),
        Milestone(babyId = babyId, category = "MOTOR_GROSS", title = "Fica em pé apoiado", description = "Fica ereto segurando em móveis ou mãos dos cuidadores", targetAgeMonths = 10),
        Milestone(babyId = babyId, category = "MOTOR_GROSS", title = "Dá os primeiros passos", description = "Cinha de 2 a 3 passos sozinho sem se apoiar", targetAgeMonths = 12),

        // Motor Fino
        Milestone(babyId = babyId, category = "MOTOR_FINE", title = "Abre as mãos espontaneamente", description = "As mãos não ficam constantemente fechadas em punho", targetAgeMonths = 2),
        Milestone(babyId = babyId, category = "MOTOR_FINE", title = "Segura um chocalho", description = "Pega um objeto leve oferecido na mão", targetAgeMonths = 4),
        Milestone(babyId = babyId, category = "MOTOR_FINE", title = "Transfere brinquedo entre mãos", description = "Passa um objeto da mão esquerda para a direita", targetAgeMonths = 6),
        Milestone(babyId = babyId, category = "MOTOR_FINE", title = "Movimento de pinça", description = "Pega um pedaço pequeno de comida usando polegar e indicador", targetAgeMonths = 9),

        // Cognitivo
        Milestone(babyId = babyId, category = "COGNITIVE", title = "Acompanha objetos em movimento", description = "Acompanha visualmente pessoas ou brinquedos a 180°", targetAgeMonths = 3),
        Milestone(babyId = babyId, category = "COGNITIVE", title = "Procura por objeto escondido", description = "Entende a permanência do objeto e busca sob uma fraldinha", targetAgeMonths = 8),
        Milestone(babyId = babyId, category = "COGNITIVE", title = "Coloca objetos em recipientes", description = "Brinca de colocar e tirar potes ou blocos", targetAgeMonths = 12),

        // Social
        Milestone(babyId = babyId, category = "SOCIAL", title = "Primeiro sorriso social", description = "Sorri em resposta ao rosto ou voz da mãe/pai", targetAgeMonths = 2),
        Milestone(babyId = babyId, category = "SOCIAL", title = "Demonstra afeição", description = "Estica os braços querendo colo e demonstra preferência por cuidadores", targetAgeMonths = 6),
        Milestone(babyId = babyId, category = "SOCIAL", title = "Faz tchau ou palminhas", description = "Imita gestos sociais conhecidos", targetAgeMonths = 9),

        // Linguagem
        Milestone(babyId = babyId, category = "LANGUAGE", title = "Emite sons (gu-gu, dada)", description = "Vocaliza vogais e sílabas simples", targetAgeMonths = 3),
        Milestone(babyId = babyId, category = "LANGUAGE", title = "Balbucio duplicado (ma-ma, ba-ba)", description = "Combina consoantes e vogais repetidas", targetAgeMonths = 7),
        Milestone(babyId = babyId, category = "LANGUAGE", title = "Primeira palavra com sentido", description = "Diz mama/papa se referindo à pessoa certa", targetAgeMonths = 12)
    )

    // --- Dados do Módulo de Gravidez por Semana ---
    fun getPregnancyWeeksData(): Map<Int, com.example.data.model.PregnancyWeekInfo> {
        val list = listOf(
            com.example.data.model.PregnancyWeekInfo(4, "Semente de Papoula", "🌱", 0.1, 0.5, "Início da implantação do blastocisto no útero e formação do tubo neural.", "Atraso menstrual, leve cansaço e sensibilidade nas mamas."),
            com.example.data.model.PregnancyWeekInfo(8, "Framboesa", "🍓", 1.6, 1.0, "Desenvolvimento dos dedinhos das mãos e pés, além dos batimentos cardíacos bem acelerados.", "Enjoos matinais, azia e sonolência."),
            com.example.data.model.PregnancyWeekInfo(12, "Limão Taiti", "🍋", 5.4, 14.0, "Reflexos em formação! O bebê já abre e fecha os dedos e engole líquido amniótico.", "Apetite retornando e redução das dores e enjoos."),
            com.example.data.model.PregnancyWeekInfo(16, "Abacate", "🥑", 11.6, 100.0, "A pele ainda é transparente, mas o bebê já consegue mover o pescoço e fazer caretas.", "Sensação de energia renovada, início do 2º trimestre."),
            com.example.data.model.PregnancyWeekInfo(20, "Banana", "🍌", 25.6, 300.0, "Metade do caminho! O bebê ouve a voz da mãe e os sons do seu coração.", "Primeiras mexidas e borboletas na barriga!"),
            com.example.data.model.PregnancyWeekInfo(24, "Espiga de Milho", "🌽", 30.0, 600.0, "Paladar se desenvolvendo e pulmões treinando movimentos de respiração.", "Inchaço leve nos pés e necessidade de hidratação intensa."),
            com.example.data.model.PregnancyWeekInfo(28, "Berinjela", "🍆", 37.6, 1000.0, "Início do 3º trimestre! O bebê abre os olhos e percebe luzes externas.", "Contrações de treinamento (Braxton Hicks) esporádicas."),
            com.example.data.model.PregnancyWeekInfo(32, "Couve-Flor", "🥦", 42.4, 1700.0, "Acúmulo de gordura subcutânea e unhas completas crescendo nas mãozinhas.", "Falta de ar com esforço e vontade frequente de fazer xixi."),
            com.example.data.model.PregnancyWeekInfo(36, "Melão", "🍈", 47.4, 2600.0, "O bebê começa a se encaixar na bacia para se preparar para o nascimento.", "Sensação de pressão pélvica e organização final da mala."),
            com.example.data.model.PregnancyWeekInfo(40, "Melancia Baby", "🍉", 51.2, 3400.0, "Prontinho para vir ao mundo! Qualquer momento é hora de conhecer o amor da sua vida.", "Ansiedade boa, tampão mucoso e monitoramento de contrações.")
        )
        return list.associateBy { it.week }
    }

    // --- Mala da Maternidade Inicial ---
    fun getDefaultHospitalBag(): List<com.example.data.model.HospitalBagItem> = listOf(
        com.example.data.model.HospitalBagItem("m1", "Mãe", "3 Camisolas ou pijamas com abertura frontal para amamentar", true),
        com.example.data.model.HospitalBagItem("m2", "Mãe", "2 Sutiãs de amamentação e absorventes de seio", true),
        com.example.data.model.HospitalBagItem("m3", "Mãe", "Produtos de higiene pessoal e chinelo para o banho", false),
        com.example.data.model.HospitalBagItem("b1", "Bebê", "6 Trocas de roupas completas (bodys, mijões e macacões)", true),
        com.example.data.model.HospitalBagItem("b2", "Bebê", "2 Mantas antialérgicas e fraldinhas de pano/boca", true),
        com.example.data.model.HospitalBagItem("b3", "Bebê", "1 Pacote de fraldas RN / P e sabonete neutro", true),
        com.example.data.model.HospitalBagItem("b4", "Bebê", "Roupa para a Saída da Maternidade", false),
        com.example.data.model.HospitalBagItem("a1", "Acompanhante", "Trocas de roupa confortáveis e casaco", false),
        com.example.data.model.HospitalBagItem("d1", "Documentos", "RG/CPF dos pais, Cartão do Pré-Natal e Carteirinha do Plano", true)
    )

    // --- Exames Pré-Natal Padrão ---
    fun getDefaultPrenatalExams(): List<com.example.data.model.PrenatalExam> = listOf(
        com.example.data.model.PrenatalExam("ex1", "Ultrassom Transvaginal Inicial", "6 a 8 semanas", true, "Confirmado batimento cardíaco do embrião"),
        com.example.data.model.PrenatalExam("ex2", "Ultrassom Morfológico do 1º Trimestre (TN)", "11 a 14 semanas", true, "Translucência nucal normal"),
        com.example.data.model.PrenatalExam("ex3", "Exames de Sangue e Sorologias (1º Trimestre)", "12 semanas", true, "Tipo sanguíneo A+ e ferro ok"),
        com.example.data.model.PrenatalExam("ex4", "Ultrassom Morfológico do 2º Trimestre", "20 a 24 semanas", true, "Anatomia perfeita e descoberta do sexo"),
        com.example.data.model.PrenatalExam("ex5", "Teste de Curva Glicêmica (TOTG)", "24 a 28 semanas", false, "Glicemia de jejum e pós-prandial"),
        com.example.data.model.PrenatalExam("ex6", "Escore de Estreptococo do Grupo B (GBS)", "35 a 37 semanas", false, "Swab vaginal e anal antes do parto")
    )

    // --- Entradas Iniciais para o Diário da Mamãe & Livro de Memórias ---
    fun getDefaultMomJournalEntries(): List<com.example.data.model.MomJournalEntry> = emptyList()
}

