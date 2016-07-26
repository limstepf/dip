# -*- coding: utf-8 -*-
"""
Material Design Icons CSS to Java Enum. Extracts named glyphs from the
official CSS file.

Get latest file from:
https://materialdesignicons.com/
"""
import re

# configuration
inputFile = './materialdesignicons.css'
outputFile = './extract-materialdesignicons-glyphs.java'


# regex pattern to parse font-awesome.css line by line using
# match (i.e. from the beginning of the line!)
glyphPattern = re.compile('\.mdi-([\w-]*):before')
unicodePattern = re.compile(' *content: "\\\\F(.*)"')

# put named glyphs into the dictionary. Multiple names can map
# to the same unicode char.
def registerGlyphs(glyphs, value):
    for g in glyphs:
        namedGlyphs[g] = value


# since we're getting rid of the '.mdi' prefix, we might end up
# with keys starting with a number, so we prefix those guys with
# an underscore to get a safe identifier for an enum.
def getSafeKey(key):
    key = key.replace('-', '_')
    if key[0].isdigit():
        return '_{0}'.format(key)
    else:
        return key


# write as java enum
def writeGlyphs(glyphs, out):
    file = open(out, 'w')
    file.write('public static enum Glyphs {\n');
    i = 0
    last = len(glyphs)
    for key in sorted(glyphs):
        file.write(
            '\t{0}(\'\\uf{1}\')'.format(
                getSafeKey(key).upper(), 
                glyphs[key].upper()
            )
        )
        i += 1
        if i == last:
            file.write(';\n')
        else:
            file.write(',\n')
    file.write('}\n');
    file.close()
     
    
# parse file     
namedGlyphs = {}
glyphBuffer = []

ok = 0
unique = 0
error = 0

for line in open(inputFile, 'r'):
    # try to read glyph name; there can be multiple names
    # for the same glyph/unicode char!
    m = glyphPattern.match(line)
    if m:
        glyphBuffer.append(m.group(1))
        continue
    
    # read unicode of glyphs if buffer isn't empty
    num = len(glyphBuffer)
    if num > 0:
        m = unicodePattern.match(line)
        if m:
            registerGlyphs(glyphBuffer, m.group(1))
            ok += num
            unique += 1
            del glyphBuffer[:]
        else:
            print 'WARNING: no content/unicode for {0}'.format(glyphBuffer)
            print line
            error += num
            del glyphBuffer[:]

print 'Done.'
print 'Glyphs ok:    \t{0} (unique: {1})'.format(ok, unique)
print 'Glyphs error: \t{0}'.format(error)
print 'Glyphs total: \t{0}'.format(ok + error)

print '\nWriting to disk...'
writeGlyphs(namedGlyphs, outputFile)

print 'Done.'
print 'kthxbai.'
