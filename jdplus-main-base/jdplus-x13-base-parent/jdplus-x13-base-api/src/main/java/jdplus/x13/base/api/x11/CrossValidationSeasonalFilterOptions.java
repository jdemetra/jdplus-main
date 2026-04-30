/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.x13.base.api.x11;

/**
 *
 * @author Christiane Hofer
 */
public enum CrossValidationSeasonalFilterOptions {
    All(new SeasonalFilterOption[]{SeasonalFilterOption.S3X1, SeasonalFilterOption.S3X3, SeasonalFilterOption.S3X5, SeasonalFilterOption.S3X9, SeasonalFilterOption.S3X15}),
    Default(new SeasonalFilterOption[]{SeasonalFilterOption.S3X3, SeasonalFilterOption.S3X5, SeasonalFilterOption.S3X9, SeasonalFilterOption.S3X15}),
    Short(new SeasonalFilterOption[]{SeasonalFilterOption.S3X3, SeasonalFilterOption.S3X5, SeasonalFilterOption.S3X9});

    private final SeasonalFilterOption[] value;

    private CrossValidationSeasonalFilterOptions(SeasonalFilterOption[] value) {
        this.value = value;

    }

    public SeasonalFilterOption[] getValue() {
        return value.clone();
    }

}
