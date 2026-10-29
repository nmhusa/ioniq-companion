# Keep runtime decoding out of optimizer constant folding; names may still be obfuscated.
-keep,allowobfuscation class com.ioniq.widget.ClientCredential {
    static java.lang.String decode(int[], int[]);
}
# Manifest components and Android callbacks are covered by AGP/default rules.
