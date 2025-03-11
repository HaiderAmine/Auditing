with open("app/src/main/java/myApps/Auditing/DirectoryActivity.java", "r+") as f:
  data = f.read()

class Bracket():
  def __init__(s, char):
    s.char = char;

brackets = []
i = 0;
ln = 1;
while (i<len(data)):
  if data[i] == '{':
    brackets.append(('{', i, ln))
  elif (data[i] == '}'):
    brackets.pop()
  elif (data[i] == '\n'):
    ln+=1;
  i+=1;
for brac in brackets:
  print(brac)
