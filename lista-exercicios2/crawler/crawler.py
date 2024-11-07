import requests
from bs4 import BeautifulSoup
import time

def get_price(url):
    try:
        response = requests.get(url)
        response.raise_for_status()
        soup = BeautifulSoup(response.text, 'html.parser')

        # Ajuste os seletores CSS de acordo com o HTML da página do produto
        # Aqui vamos adicionar condições para diferentes sites
        if 'fujioka' in url:
            price = soup.find('div', {'class': 'productPage__best-price'}).get_text(strip=True)
        elif 'catral' in url:
            price = soup.find('strong', {'class': 'skuBestPrice'}).get_text(strip=True)
        else:
            print("URL não suportada.")
            return None

        return price
    except Exception as e:
        print(f"Erro ao buscar preço em {url}: {e}")
        return None

# Lista de URLs dos produtos que deseja monitorar
product_urls = [
    'https://www.fujioka.com.br/smart-tv-philips-55-4k-uhd-led-55pug790878-ambilight-dolby-vision-dolby-atmos-bluetooth-wifi-4-hdmi-2-usb-67560/p',  # URL do produto na Fujioka
    'https://www.catral.com.br/arcondicionado-split-top-auto-inverter-gree/p?uam=true&mobile=4',  # URL do produto na Catral
    'https://www.fujioka.com.br/impressora-multifuncional-epson-ecotank-l3250-wireless-wifi-direct-265105/p',
    'https://www.fujioka.com.br/purificador-parede-eletrolux-experience-pc01-compressor-agua-fria-natural-gelada-filtra-sem-energia-ambientes-199053/p'
]

# Loop contínuo para verificar os preços a cada 5 minutos
while True:
    for url in product_urls:
        price = get_price(url)
        if price:
            print(f"Preço de {url}: {price}")
        else:
            print(f"Não foi possível obter o preço para {url}")

    # Espera 5 minutos antes de verificar novamente
   time.sleep(300)