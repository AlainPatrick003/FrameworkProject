@echo off

mkdir "out"

for /r "src" %%f in (*.java) do copy "%%f" "out"

@REM Compiler toutes les classes en spécifiant le classpath
for %%f in (out\*.java) do (
    javac -cp "lib\*" -d "." "%%f"
)

echo "construction du fichier jar"
jar cfe "lib\front-controller.jar" -c "mg"
echo "construction terminer"

if exist "out" (
    rmdir /s /q "out"
)
if exist "mg" (
    rmdir /s /q "mg"
)
