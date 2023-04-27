USE `idservice`;

CREATE DEFINER = `dev_idservice`@`%` TRIGGER `del_schemeId`
    BEFORE DELETE
    ON `schemeId`
    FOR EACH ROW
BEGIN
    INSERT INTO `schemeId_log` (`scheme`,
                                `schemeId`,
                                `sequence`,
                                `checkDigit`,
                                `systemId`,
                                `status`,
                                `author`,
                                `software`,
                                `expirationDate`,
                                `comment`,
                                `jobId`,
                                `created_at`,
                                `modified_at`)
    VALUES (`OLD`.`scheme`,
            `OLD`.`schemeId`,
            `OLD`.`sequence`,
            `OLD`.`checkDigit`,
            `OLD`.`systemId`,
            `OLD`.`status`,
            `OLD`.`author`,
            `OLD`.`software`,
            `OLD`.`expirationDate`,
            `OLD`.`comment`,
            `OLD`.`jobId`,
            `OLD`.`created_at`,
            `OLD`.`modified_at`);
END;

CREATE DEFINER = `dev_idservice`@`%` TRIGGER `upd_schemeId`
    BEFORE UPDATE
    ON `schemeId`
    FOR EACH ROW
BEGIN
    INSERT INTO `schemeId_log` (`scheme`,
                                `schemeId`,
                                `sequence`,
                                `checkDigit`,
                                `systemId`,
                                `status`,
                                `author`,
                                `software`,
                                `expirationDate`,
                                `comment`,
                                `jobId`,
                                `created_at`,
                                `modified_at`)
    VALUES (`OLD`.`scheme`,
            `OLD`.`schemeId`,
            `OLD`.`sequence`,
            `OLD`.`checkDigit`,
            `OLD`.`systemId`,
            `OLD`.`status`,
            `OLD`.`author`,
            `OLD`.`software`,
            `OLD`.`expirationDate`,
            `OLD`.`comment`,
            `OLD`.`jobId`,
            `OLD`.`created_at`,
            `OLD`.`modified_at`);
END;

CREATE DEFINER = `dev_idservice`@`%` TRIGGER `del_sctId`
    BEFORE DELETE
    ON `sctId`
    FOR EACH ROW
BEGIN
    INSERT INTO `sctId_log` (`sctid`,
                             `sequence`,
                             `namespace`,
                             `partitionId`,
                             `checkDigit`,
                             `systemId`,
                             `status`,
                             `author`,
                             `software`,
                             `expirationDate`,
                             `comment`,
                             `jobId`,
                             `created_at`,
                             `modified_at`)
    VALUES (`OLD`.`sctid`,
            `OLD`.`sequence`,
            `OLD`.`namespace`,
            `OLD`.`partitionId`,
            `OLD`.`checkDigit`,
            `OLD`.`systemId`,
            `OLD`.`status`,
            `OLD`.`author`,
            `OLD`.`software`,
            `OLD`.`expirationDate`,
            `OLD`.`comment`,
            `OLD`.`jobId`,
            `OLD`.`created_at`,
            `OLD`.`modified_at`);
END;

CREATE DEFINER = `dev_idservice`@`%` TRIGGER `upd_sctId`
    BEFORE UPDATE
    ON `sctId`
    FOR EACH ROW
BEGIN
    INSERT INTO `sctId_log` (`sctid`,
                             `sequence`,
                             `namespace`,
                             `partitionId`,
                             `checkDigit`,
                             `systemId`,
                             `status`,
                             `author`,
                             `software`,
                             `expirationDate`,
                             `comment`,
                             `jobId`,
                             `created_at`,
                             `modified_at`)
    VALUES (`OLD`.`sctid`,
            `OLD`.`sequence`,
            `OLD`.`namespace`,
            `OLD`.`partitionId`,
            `OLD`.`checkDigit`,
            `OLD`.`systemId`,
            `OLD`.`status`,
            `OLD`.`author`,
            `OLD`.`software`,
            `OLD`.`expirationDate`,
            `OLD`.`comment`,
            `OLD`.`jobId`,
            `OLD`.`created_at`,
            `OLD`.`modified_at`);
END;
