#
# Copyright (C) 2024 CrowdWare
#
# This file is part of NoCodeDesigner.
#
#  NoCodeDesigner is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  NoCodeDesigner is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with NoCodeDesigner.  If not, see <http://www.gnu.org/licenses/>.
#

import os
import re
import socket
from datetime import datetime
from http.server import SimpleHTTPRequestHandler, HTTPServer

def get_local_ip_address():
    """Ermittelt die lokale IP-Adresse des Geräts."""
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        # Verbindung zu externem Host, um lokale IP zu ermitteln
        s.connect(("8.8.8.8", 80))
        local_ip_address = s.getsockname()[0]
    except Exception:
        local_ip_address = "Unable to get IP address"
    finally:
        s.close()
    return local_ip_address

# Funktion, um die Deployment-Daten einzufügen oder zu ersetzen
def update_deployment_in_app_sml(app_sml_path, deployment_data):
    # Lese den Inhalt der App.sml
    with open(app_sml_path, 'r') as file:
        app_sml_content = file.read()

    # Regex, um den Deployment-Bereich zu finden
    deployment_section_regex = re.compile(r"// deployment start.*?// deployment end", re.DOTALL)

    # Deployment Block als String vorbereiten
    deployment_block = f"""// deployment start - don't edit here
Deployment {{
{deployment_data}
}}
// deployment end"""

    # Überprüfen, ob ein Deployment-Bereich vorhanden ist
    if deployment_section_regex.search(app_sml_content):
        # Deployment-Bereich ersetzen
        app_sml_content = deployment_section_regex.sub(deployment_block, app_sml_content)
    else:
        # Kein Deployment-Bereich vorhanden, vor der letzten Klammer einfügen
        app_sml_content = app_sml_content.rstrip('}') + '\n' + deployment_block + '\n}'

    # Aktualisiere die App.sml Datei
    with open(app_sml_path, 'w') as file:
        file.write(app_sml_content)

# Funktion, um die Dateien in den Verzeichnissen zu scannen und Deployment-Daten zu generieren
def generate_deployment_data(base_path, exclude_files=None):
    if exclude_files is None:
        exclude_files = []

    deployment_entries = []
    for dirpath, _, filenames in os.walk(base_path):
        for filename in filenames:
            if filename not in exclude_files and not filename.startswith('.'):
                # Dateipfad relativ zum Base-Pfad
                file_path = os.path.relpath(os.path.join(dirpath, filename), base_path)
                # Letztes Änderungsdatum der Datei
                mod_time = os.path.getmtime(os.path.join(dirpath, filename))
                # Formatiertes Datum
                formatted_time = datetime.utcfromtimestamp(mod_time).strftime('%Y.%m.%d %H.%M.%S')
                # Deployment-Eintrag hinzufügen
                deployment_entries.append(f'  File {{ path: "{file_path}" time: "{formatted_time}" }}')

    return "\n".join(deployment_entries)

def start_web_server(port=8000):
    """Startet einen HTTP-Webserver."""
    handler = SimpleHTTPRequestHandler
    server = HTTPServer(("", port), handler)

    ip_address = get_local_ip_address()
    if ip_address:
        print(f"Server running at: http://{ip_address}:{port}/")

    print(f"Serving on port {port}")
    server.serve_forever()


if __name__ == "__main__":
    base_path = os.getcwd()
    app_sml_path = os.path.join(base_path, 'App.sml')

    # Verzeichnisse, die gescannt werden sollen
    pages_path = os.path.join(base_path, 'pages')
    assets_path = os.path.join(base_path, 'assets')

    # Generiere Deployment-Daten für beide Verzeichnisse

    deployment_data = generate_deployment_data(pages_path, exclude_files=['.DS_Store'])
    deployment_data += "\n" + generate_deployment_data(assets_path, exclude_files=['.DS_Store'])

    print("Updating App.sml with deployment files...")
    update_deployment_in_app_sml(app_sml_path, deployment_data)

    print("Starting the web server...")
    start_web_server(port=8000)