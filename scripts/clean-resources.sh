echo "Removing old built frontend from resources"
rm -R src/main/resources/frontend || true
echo "Removing old build common library from resources"
rm -R src/main/resources/admin/aster-common || true
